package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.WalletHourlyCapture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WalletHourlyCaptureRepository extends JpaRepository<WalletHourlyCapture, String> {
    @Query(value = "SELECT whc FROM WalletHourlyCapture whc WHERE whc.isEndOfDay = true and whc.captureTime >= :startDate and whc.captureTime <= :toDate")
    List<WalletHourlyCapture> getWalletDailyCaptureByDateRange(String startDate, String toDate);

    @Query(value = "SELECT whc FROM WalletHourlyCapture whc WHERE whc.isEndOfWeek = true and whc.captureTime >= :startDate and whc.captureTime <= :toDate")
    List<WalletHourlyCapture> getWalletWeeklyCaptureByDateRange(String startDate, String toDate);

    @Query(value = "SELECT whc FROM WalletHourlyCapture whc WHERE whc.isEndOfMonth = true and whc.captureTime >= :startDate and whc.captureTime <= :toDate")
    List<WalletHourlyCapture> getWalletMonthlyCaptureByDateRange(String startDate, String toDate);

    @Query(value = "SELECT whc FROM WalletHourlyCapture whc WHERE whc.captureTime >= :startDate and whc.captureTime <= :toDate")
    List<WalletHourlyCapture> getWalletHourlyCaptureByDateRange(String startDate, String toDate);
}
