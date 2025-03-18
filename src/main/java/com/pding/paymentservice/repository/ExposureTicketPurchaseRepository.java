package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.ExposureTicketPurchase;
import com.pding.paymentservice.models.enums.ExposureTicketStatus;
import com.pding.paymentservice.models.enums.ExposureTicketType;
import com.pding.paymentservice.payload.dto.UserTicketCountDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

public interface ExposureTicketPurchaseRepository extends JpaRepository<ExposureTicketPurchase, String> {
    Page<ExposureTicketPurchase> findByUserId(String userId, Pageable pageable);
    List<ExposureTicketPurchase> findByUserId(String userId);
    Optional<ExposureTicketPurchase> findFirstByTypeAndStatusAndUserId(ExposureTicketType type, ExposureTicketStatus status, String userId);
    Long countByTypeAndUserIdAndStatus(ExposureTicketType type, String userId, ExposureTicketStatus status);

    @Query(value = "SELECT COALESCE(SUM(vp.treesConsumed), 0) FROM ExposureTicketPurchase vp WHERE vp.userId = :userId and vp.status != 'REFUNDED'")
    BigDecimal getTotalTreesConsumedByUserId(String userId);


    @Query(value =
        " select date(purchased_date) offer_date, count(*) as number_of_offer_ticket" +
        " from exposure_ticket_purchase" +
        " where user_id = :userId and type = :type" +
        " group by offer_date" +
        " order by offer_date desc", nativeQuery = true)
    Page<Object[]> _countUserTicketByDate(String userId, String type, Pageable pageable);

    default Page<UserTicketCountDTO> countUserTicketByDate(String userId, String type, Pageable pageable) {
        return _countUserTicketByDate(userId, type, pageable).map(row -> {
            UserTicketCountDTO userTicketCountDTO = new UserTicketCountDTO();
            userTicketCountDTO.setDate(((Date)row[0]).toLocalDate());
            userTicketCountDTO.setCount(Integer.valueOf(row[1].toString()));
            return userTicketCountDTO;
        });
    }

}
