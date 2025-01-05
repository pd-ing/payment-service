package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.MExposureSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExposureSlotRepository extends JpaRepository<MExposureSlot, String> {
}
