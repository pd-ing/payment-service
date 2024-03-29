package com.pding.paymentservice.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.ssm.endpoints.internal.Value;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClearPendingAndStalePaymentsResponse {
    String userId;
    String emailId;
    String purchasedDate;
    String sessionId;
    String paymentIntentId;
    Boolean stripeSessionPaymentStatus;
    String backendPaymentStatus;
}
