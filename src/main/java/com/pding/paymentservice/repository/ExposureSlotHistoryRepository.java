package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.MExposureSlotHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExposureSlotHistoryRepository extends JpaRepository<MExposureSlotHistory, String> {
}
