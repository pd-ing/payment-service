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
public class AddLeafsRequest {

    @NotNull(message = "Email ID cannot be null.")
    String email;
    String productId;
    String purchaseToken;
}
