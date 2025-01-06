package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.ExposureTicketPurchase;
import com.pding.paymentservice.models.enums.ExposureTicketStatus;
import com.pding.paymentservice.models.enums.ExposureTicketType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExposureTicketPurchaseRepository extends JpaRepository<ExposureTicketPurchase, String> {
    Page<ExposureTicketPurchase> findByUserId(String userId, Pageable pageable);
    Optional<ExposureTicketPurchase> findFirstByTypeAndStatusAndUserId(ExposureTicketType type, ExposureTicketStatus status, String userId);
}
