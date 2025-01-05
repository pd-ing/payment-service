package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.MExposureSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExposureSlotRepository extends JpaRepository<MExposureSlot, String> {
    Optional<MExposureSlot> findByUserId(String userId);
}
