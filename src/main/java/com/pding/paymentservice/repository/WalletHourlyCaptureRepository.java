package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.WalletHourlyCapture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public interface WalletHourlyCaptureRepository extends JpaRepository<WalletHourlyCapture, String> {
    @Query(value = "SELECT whc FROM WalletHourlyCapture whc WHERE whc.isEndOfDay = true and whc.captureTime >= :startDate and whc.captureTime <= :toDate")
    List<WalletHourlyCapture> getWalletDailyCaptureByDateRange(ZonedDateTime startDate, ZonedDateTime toDate);

    @Query(value = "SELECT whc FROM WalletHourlyCapture whc WHERE whc.isEndOfWeek = true and whc.captureTime >= :startDate and whc.captureTime <= :toDate")
    List<WalletHourlyCapture> getWalletWeeklyCaptureByDateRange(ZonedDateTime startDate, ZonedDateTime toDate);

    @Query(value = "SELECT whc FROM WalletHourlyCapture whc WHERE whc.isEndOfMonth = true and whc.captureTime >= :startDate and whc.captureTime <= :toDate")
    List<WalletHourlyCapture> getWalletMonthlyCaptureByDateRange(ZonedDateTime startDate, ZonedDateTime toDate);

    @Query(value = "SELECT whc FROM WalletHourlyCapture whc WHERE whc.captureTime >= :startDate and whc.captureTime <= :toDate")
    List<WalletHourlyCapture> getWalletHourlyCaptureByDateRange(ZonedDateTime startDate, ZonedDateTime toDate);

    @Query(value = "SELECT whc FROM WalletHourlyCapture whc WHERE DATE(whc.captureTime) = :selectedDate")
    List<WalletHourlyCapture> getWalletHourlyCaptureByDate(LocalDate selectedDate);
}
