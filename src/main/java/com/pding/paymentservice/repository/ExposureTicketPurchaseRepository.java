package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.ExposureTicketPurchase;
import com.pding.paymentservice.models.enums.ExposureTicketStatus;
import com.pding.paymentservice.models.enums.ExposureTicketType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface ExposureTicketPurchaseRepository extends JpaRepository<ExposureTicketPurchase, String> {
    Page<ExposureTicketPurchase> findByUserId(String userId, Pageable pageable);
    Optional<ExposureTicketPurchase> findFirstByTypeAndStatusAndUserId(ExposureTicketType type, ExposureTicketStatus status, String userId);
    Long countByTypeAndUserIdAndStatus(ExposureTicketType type, String userId, ExposureTicketStatus status);

    @Query(value = "SELECT COALESCE(SUM(vp.treesConsumed), 0) FROM ExposureTicketPurchase vp WHERE vp.userId = :userId and vp.status != 'REFUNDED'")
    BigDecimal getTotalTreesConsumedByUserId(String userId);
}
