package com.pding.paymentservice.repository.admin;

import com.pding.paymentservice.models.VideoPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RealTimeLeavesUsageTabRepository extends JpaRepository<VideoPurchase, String> {

    @Query(value = "select COALESCE(SUM(leafs_transacted), 0)" +
            " from call_purchase" +
            " where call_type = :callType" +
            "  AND (:startDate IS NULL OR last_update_date >= :startDate)" +
            "  AND (:endDate IS NULL OR last_update_date <= :endDate)", nativeQuery = true)
    BigDecimal getTotalLeavesUsedForCall(
            @Param("callType") String callType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = "select COALESCE(SUM(leafs_transacted), 0)" +
            " from message_purchase" +
            " where gift_id is null" +
            "   AND (:startDate IS NULL OR last_update_date >= :startDate)" +
            "   AND (:endDate IS NULL OR last_update_date <= :endDate)", nativeQuery = true)
    BigDecimal getTotalLeavesUsedForChat(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = "select COALESCE(SUM(leafs_transacted), 0) as totalGiftInChat" +
            " from message_purchase" +
            " where gift_id is not null" +
            "   AND (:startDate IS NULL OR last_update_date >= :startDate)" +
            "   AND (:endDate IS NULL OR last_update_date <= :endDate)", nativeQuery = true)
    BigDecimal getTotalGiftInChat(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = "select COALESCE(SUM(leafs_transacted), 0) as totalGiftInCall" +
            " from call_purchase" +
            " where gift_id is not null" +
            "   AND (:startDate IS NULL OR last_update_date >= :startDate)" +
            "   AND (:endDate IS NULL OR last_update_date <= :endDate)", nativeQuery = true)
    BigDecimal getTotalGiftInCall(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = "select COALESCE(SUM(leafs_to_charge), 0)" +
            " from in_chat_media_trading" +
            " where transaction_status = 'PAID'" +
            "   AND (:startDate IS NULL OR last_update_date >= :startDate)" +
            "   AND (:endDate IS NULL OR last_update_date <= :endDate)", nativeQuery = true)
    BigDecimal getTotalLeavesUsedForInChatMediaTrading(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    @Query(
            value =
                    "         select userId, userEmail, transactionDateTime, transactionType, totalLeaves, pdUserId, pdNickName" +
                            " from (select cp.user_id                as userId," +
                            "              u.email                   as userEmail," +
                            "              cp.last_update_date       as transactionDateTime," +
                            "              (case" +
                            "                   when" +
                            "                       cp.gift_id is null then cp.call_type" +
                            "                   else 'GIFT'" +
                            "                  end" +
                            "                  )                     as transactionType," +
                            "              cp.leafs_transacted       as totalLeaves," +
                            "              cp.pd_user_id             as pdUserId," +
                            "              COALESCE(pd.nickname, '') as pdNickName" +
                            "       from call_purchase cp" +
                            "                left join users u on u.id = cp.user_id" +
                            "                left join users pd on pd.id = cp.pd_user_id" +
                            "       UNION ALL" +
                            "       select mp.user_id                as userId," +
                            "              u.email                   as userEmail," +
                            "              mp.last_update_date       as transactionDateTime," +
                            "              (case" +
                            "                   when" +
                            "                       mp.gift_id is null then 'MESSAGE'" +
                            "                   else 'GIFT'" +
                            "                  end" +
                            "                  )                     as transactionType," +
                            "              mp.leafs_transacted       as totalLeaves," +
                            "              mp.pd_userid              as pdUserId," +
                            "              COALESCE(pd.nickname, '') as pdNickName" +
                            "       from message_purchase mp" +
                            "                left join users u on u.id = mp.user_id" +
                            "                left join users pd on pd.id = mp.pd_userid" +
                            "       UNION ALL" +
                            "       select mt.user_id                as userId," +
                            "              u.email                   as userEmail," +
                            "              mt.last_update_date       as transactionDateTime," +
                            "              'IN_CHAT_MEDIA_TRADING'   as transactionType," +
                            "              mt.leafs_to_charge        as totalLeaves," +
                            "              mt.pd_id                  as pdUserId," +
                            "              COALESCE(pd.nickname, '') as pdNickName" +
                            "       from in_chat_media_trading mt" +
                            "                left join users u on u.id = mt.user_id" +
                            "                left join users pd on pd.id = mt.pd_id" +
                            "       where mt.transaction_status = 'PAID') tb" +
                            " where (:startDate IS NULL OR transactionDateTime >= :startDate) " +
                            " AND (:endDate IS NULL OR transactionDateTime <= :endDate) " +
                            " AND (:searchString IS NULL OR (userEmail LIKE concat('%', :searchString, '%') OR pdNickName LIKE concat(:searchString, '%')))  ",
            countQuery =
                    "         select userId, userEmail, transactionDateTime, transactionType, totalLeaves, pdUserId, pdNickName" +
                            " from (select cp.user_id                as userId," +
                            "              u.email                   as userEmail," +
                            "              cp.last_update_date       as transactionDateTime," +
                            "              (case" +
                            "                   when" +
                            "                       cp.gift_id is null then cp.call_type" +
                            "                   else 'GIFT'" +
                            "                  end" +
                            "                  )                     as transactionType," +
                            "              cp.leafs_transacted       as totalLeaves," +
                            "              cp.pd_user_id             as pdUserId," +
                            "              COALESCE(pd.nickname, '') as pdNickName" +
                            "       from call_purchase cp" +
                            "                left join users u on u.id = cp.user_id" +
                            "                left join users pd on pd.id = cp.pd_user_id" +
                            "       UNION ALL" +
                            "       select mp.user_id                as userId," +
                            "              u.email                   as userEmail," +
                            "              mp.last_update_date       as transactionDateTime," +
                            "              (case" +
                            "                   when" +
                            "                       mp.gift_id is null then 'MESSAGE'" +
                            "                   else 'GIFT'" +
                            "                  end" +
                            "                  )                     as transactionType," +
                            "              mp.leafs_transacted       as totalLeaves," +
                            "              mp.pd_userid              as pdUserId," +
                            "              COALESCE(pd.nickname, '') as pdNickName" +
                            "       from message_purchase mp" +
                            "                left join users u on u.id = mp.user_id" +
                            "                left join users pd on pd.id = mp.pd_userid" +
                            "       UNION ALL" +
                            "       select mt.user_id                as userId," +
                            "              u.email                   as userEmail," +
                            "              mt.last_update_date       as transactionDateTime," +
                            "              'IN_CHAT_MEDIA_TRADING'   as transactionType," +
                            "              mt.leafs_to_charge        as totalLeaves," +
                            "              mt.pd_id                  as pdUserId," +
                            "              COALESCE(pd.nickname, '') as pdNickName" +
                            "       from in_chat_media_trading mt" +
                            "                left join users u on u.id = mt.user_id" +
                            "                left join users pd on pd.id = mt.pd_id" +
                            "       where mt.transaction_status = 'PAID') tb" +
                            " where (:startDate IS NULL OR transactionDateTime >= :startDate) " +
                            " AND (:endDate IS NULL OR transactionDateTime <= :endDate) " +
                            " AND (:searchString IS NULL OR (userEmail LIKE concat('%', :searchString, '%') OR pdNickName LIKE concat(:searchString, '%'))) ",
            nativeQuery = true
    )
    Page<Object[]> getRealTimeTreeUsage(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("searchString") String searchString,
                                        Pageable pageable);
}
