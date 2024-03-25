package com.pding.paymentservice.payload.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddOrRemoveTreesRequest {

    @NotNull(message = "User ID cannot be null.")
    String userId;

    @DecimalMin(value = "0.0", message = "Trees should be greater than 0.")
    BigDecimal trees;
}
