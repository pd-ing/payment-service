package com.pding.paymentservice.payload.response.admin;

import com.pding.paymentservice.payload.response.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreeBalanceResponse {
    ErrorResponse errorResponse;
    BigDecimal treeLeftInWalletAllUsers;
    BigDecimal treeLeftInEarningAllUsers;
    BigDecimal leafLeftInWalletAllUsers;
    BigDecimal leafLeftInEarningAllUsers;
}
