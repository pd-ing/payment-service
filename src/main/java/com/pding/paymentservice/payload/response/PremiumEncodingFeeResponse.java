package com.pding.paymentservice.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PremiumEncodingFeeResponse {
    private ErrorResponse error;
    private String message;
    private BigDecimal deductedAmount;
    private BigDecimal remainingBalance;
}
