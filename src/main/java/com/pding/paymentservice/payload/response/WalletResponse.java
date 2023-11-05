package com.pding.paymentservice.payload.response;


import com.pding.paymentservice.models.Wallet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WalletResponse {
    ErrorResponse errorResponse;
    Wallet wallet;
    BigDecimal treesEarned;
}
