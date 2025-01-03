package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.ExposureTicketPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExposureTicketPurchaseRepository extends JpaRepository<ExposureTicketPurchase, String> {
    Page<ExposureTicketPurchase> findByUserId(String userId, Pageable pageable);
}
