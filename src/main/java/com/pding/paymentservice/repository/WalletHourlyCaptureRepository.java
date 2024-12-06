package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.WalletHourlyCapture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletHourlyCaptureRepository extends JpaRepository<WalletHourlyCapture, String> {
}
