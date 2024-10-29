package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.CallPurchase;
import com.pding.paymentservice.payload.dto.LeafEarningInCallingHistoryDTO;
import com.pding.paymentservice.payload.dto.LeafGiftHistoryDTO;
import com.pding.paymentservice.payload.dto.PurchasedLeafHistoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface PaymentStatisticRepository extends JpaRepository<CallPurchase, String> {

    @Query(value =
            "select cl.call_cid                   callCid, " +
                    "        cl.user_id                    userId, " +
                    "        cl.pd_id                      pdId, " +
                    "        cl.call_charges_per_minutes   pricePerMinute, " +
                    "        cl.call_type                  category, " +
                    "        cl.duration_actual_in_seconds duration, " +
                    "        cl.ready_status_time          startTime, " +
                    "        cl.ended_status_time          endTime, " +
                    "        sum(cp.leafs_transacted) as   totalLeafs, " +
                    "        u.email                       userEmail, " +
                    "        cl.created_at " +
                    " from call_log cl " +
                    "          left join call_purchase cp on cl.call_cid = cp.call_id " +
                    "          left join users u on cl.user_id = u.id " +
                    " where call_charges_per_minutes > 0 " +
                    "   and finish_processing = true " +
                    "   and cl.pd_id = :pdId and cp.gift_id is null" +
                    "   and (:startDate is null or cl.created_at >= :startDate) " +
                    "   and (:endDate is null or cl.created_at <= :endDate) " +
                    " group by cp.call_id, cl.created_at " +
                    " order by cl.created_at desc",
            nativeQuery = true)
    Page<Object[]> _getLeafEarningInCallingHistory(String pdId, String startDate, String endDate, Pageable pageable);

    default Page<LeafEarningInCallingHistoryDTO> getLeafEarningInCallingHistory(String pdId, String startDate, String endDate, Pageable pageable) {
        return _getLeafEarningInCallingHistory(pdId, startDate, endDate, pageable)
                .map(objects -> {
                    LeafEarningInCallingHistoryDTO dto = new LeafEarningInCallingHistoryDTO();
                    dto.setCallId((String) objects[0]);
                    dto.setUserId((String) objects[1]);
                    dto.setPdId((String) objects[2]);
                    dto.setPricePerMinute((Float) objects[3]);
                    dto.setCategory((String) objects[4]);
                    dto.setDurationInSeconds((Long) objects[5]);

                    Timestamp startTime = (Timestamp) objects[6];
                    dto.setStartTime(startTime.toLocalDateTime());

                    Timestamp endTime = (Timestamp) objects[7];
                    dto.setEndTime(endTime.toLocalDateTime());

                    dto.setTotalLeafs((java.math.BigDecimal) objects[8]);
                    dto.setEmail((String) objects[9]);
                    return dto;
                });
    }

    @Query(value =
            "(select pd_userid as pdId, user_id as userId, u.email as email, last_update_date as date, leafs_transacted as leafs " +
                    "  from message_purchase " +
                    "           left join users u on user_id = u.id " +
                    "  where is_gift = true " +
                    "    and pd_userid = :pdId) " +
                    " union all " +
                    " (select pd_user_id as pdId, user_id as userId, u.email as email, last_update_date as date, leafs_transacted as leafs " +
                    "  from call_purchase " +
                    "           left join users u on user_id = u.id " +
                    "  where gift_id is not null " +
                    "    and pd_user_id = :pdId) order by date desc",
            countQuery = "select count(*) from " +
                    "(select pd_userid as pdId, user_id as userId, u.email as email, last_update_date as date, leafs_transacted as leafs " +
                    "  from message_purchase " +
                    "           left join users u on user_id = u.id " +
                    "  where is_gift = true " +
                    "    and pd_userid = :pdId " +
                    " union all " +
                    " (select pd_user_id as pdId, user_id as userId, u.email as email, last_update_date as date, leafs_transacted as leafs " +
                    "  from call_purchase " +
                    "           left join users u on user_id = u.id " +
                    "  where gift_id is not null " +
                    "    and pd_user_id = :pdId) ) as temp",
            nativeQuery = true
    )
    Page<Object[]> _getLeafEarningFromGiftHistory(String pdId, Pageable pageable);

    default Page<LeafGiftHistoryDTO> getLeafEarningFromGiftHistory(String pdId, Pageable pageable) {
        return _getLeafEarningFromGiftHistory(pdId, pageable)
                .map(objects -> {
                    LeafGiftHistoryDTO dto = new LeafGiftHistoryDTO();
                    dto.setPdId((String) objects[0]);
                    dto.setUserId((String) objects[1]);
                    dto.setUserEmail((String) objects[2]);
                    Timestamp startTime = (Timestamp) objects[3];
                    if (startTime != null) {
                        dto.setDate(startTime.toLocalDateTime());
                    }
                    dto.setLeafsTransacted((java.math.BigDecimal) objects[4]);
                    return dto;
                });
    }

    @Query(value =
            "select call_type, sum(duration_actual_in_seconds)" +
                    " from call_log" +
                    " where pd_id = :pdId" +
                    " group by call_type"
    , nativeQuery = true)
    List<Object[]> _getCallTypeDuration(String pdId);

    default Map<String, BigDecimal> getCallTypeDuration(String pdId) {
        return _getCallTypeDuration(pdId).stream()
                .collect(Collectors.toMap(
                        objects -> (String) objects[0],
                        objects -> (BigDecimal) objects[1]
                ));
    }

    @Query(value =
            "select count(*) " +
                    " from message_purchase where is_gift = false " +
                    " and pd_userid = :pdId"
    , nativeQuery = true)
    Long getTotalTextMessage(String pdId);

    @Query(value =
            "select count(*) " +
                    " from message_purchase where is_gift = true " +
                    " and pd_userid = :pdId", nativeQuery = true)
    Long getTotalGiftsInChat(String pdId);

    @Query(value =
            "select count(*) " +
                    " from call_purchase where gift_id is not null " +
                    " and pd_user_id = :pdId", nativeQuery = true)
    Long getTotalGiftsInCall(String pdId);


    @Query(value = " select wh.user_id as userId, u.email, wh.purchase_date, payment_method, purchased_leafs" +
                    " from wallet_history wh" +
                    "          left join users u on wh.user_id = u.id" +
                    " where wh.purchased_leafs > 0" +
                    "   and wh.transaction_status = 'paymentCompleted'" +
                    "   and (:searchString is null or wh.user_id like CONCAT('%', :searchString, '%') or u.email like CONCAT('%', :searchString, '%'))" +
                    "   and (:fromDate is null or wh.purchase_date >= :fromDate)" +
                    "   and (:toDate is null or wh.purchase_date <= :toDate)" +
                    "   order by wh.purchase_date desc"
            , nativeQuery = true
    )
    Page<Object[]> _getPurchasedLeafWalletHistory(String searchString, String fromDate, String toDate, Pageable pageable);

    default Page<PurchasedLeafHistoryDTO> getPurchasedLeafWalletHistory(String searchString, String fromDate, String toDate, Pageable pageable) {
        return _getPurchasedLeafWalletHistory(searchString, fromDate, toDate, pageable)
                .map(objects -> {
                    PurchasedLeafHistoryDTO dto = new PurchasedLeafHistoryDTO();
                    dto.setUserId((String) objects[0]);
                    dto.setEmail((String) objects[1]);
                    Timestamp startTime = (Timestamp) objects[2];
                    if (startTime != null) {
                        dto.setPurchaseDate(startTime.toLocalDateTime());
                    }
                    dto.setPaymentMethod((String) objects[3]);
                    dto.setLeafAmount((java.math.BigDecimal) objects[4]);
                    return dto;
                });
    }

    @Query(value =
            "select sum(purchased_leafs) " +
                    " from wallet_history " +
                    " where purchased_leafs > 0 " +
                    "   and transaction_status = 'paymentCompleted'", nativeQuery = true
    )
    BigDecimal getTotalPurchasedLeafs();

    @Query(
            value = "select sum(leafs)" +
                    " from wallet", nativeQuery = true
    )
    BigDecimal getTotalLeafsRemainingInWallet();
}
