package com.pding.paymentservice.payload.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchasedLeafHistoryDTO {
    private String userId;
    private String email;
    private LocalDateTime purchaseDate;
    private String paymentMethod;
    private BigDecimal leafAmount;
}
