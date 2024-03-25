package com.pding.paymentservice.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInitFromBackendRequest {
    @NotBlank(message = "productId cannot be blank.")
    String productId;

    @NotBlank(message = "successUrl cannot be blank.")
    String successUrl;

    @NotBlank(message = "failureUrl cannot be blank.")
    String failureUrl;
}
