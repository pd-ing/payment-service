package com.pding.paymentservice.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class VideoPurchaseLiteDTO {
    private String duration;
    private BigDecimal treesConsumed;
    private LocalDateTime purchaseDate;
    private LocalDateTime expiryDate;
}
