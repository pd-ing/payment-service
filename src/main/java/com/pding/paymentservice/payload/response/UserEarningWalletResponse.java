package com.pding.paymentservice.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response model for user's earning wallet balance
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEarningWalletResponse {
    private ErrorResponse error;
    private String message;
    private String userId;
    private BigDecimal treesEarned;
    private BigDecimal leafsEarned;
}
