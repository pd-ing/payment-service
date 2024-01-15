package com.pding.paymentservice.payload.request;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawRequest {

    @Nullable
    String pdUserId;

    @Nullable
    BigDecimal trees;

    @Nullable
    BigDecimal leafs;
}
