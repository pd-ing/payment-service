package com.pding.paymentservice.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClearPendingPaymentRequest {
    @NotNull(message = "sessionId cannot be null.")
    String sessionId;
}
