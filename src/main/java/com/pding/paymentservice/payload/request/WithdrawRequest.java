package com.pding.paymentservice.payload.request;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawRequest {

    @Nullable
    String pdUserId;

    @Nullable
    //@DecimalMin(value = "500", inclusive = true, message = "Minimum withdraw value for trees is 500")
    BigDecimal trees;

    @Nullable
    //@DecimalMin(value = "500", inclusive = true, message = "Minimum withdraw value for leafs is 500")
    BigDecimal leafs;
}
