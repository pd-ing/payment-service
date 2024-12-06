package com.pding.paymentservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "WalletHourlyCapture")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WalletHourlyCapture {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    private ZonedDateTime captureTime;
    private BigDecimal totalTreeLeftInWallet;
    private BigDecimal totalTreeLeftInEarning;

    private Boolean isEndOfDay;
    private Boolean isEndOfWeek;
    private Boolean isEndOfMonth;
//    private Boolean isEndOfYear;
}
