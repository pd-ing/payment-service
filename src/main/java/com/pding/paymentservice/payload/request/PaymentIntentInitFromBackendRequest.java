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
public class PaymentIntentInitFromBackendRequest {
    @NotBlank(message = "priceId cannot be blank.")
    String priceId;
}
