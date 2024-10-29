package com.pding.paymentservice.payload.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchasedLeafHistorySummaryDTO {
    BigDecimal totalLeafsPurchased;
    BigDecimal totalLeafsRemaining;
}
