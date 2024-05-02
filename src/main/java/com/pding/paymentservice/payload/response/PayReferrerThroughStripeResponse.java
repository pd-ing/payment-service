package com.pding.paymentservice.payload.response;

import com.stripe.model.PaymentIntent;
import com.stripe.model.Transfer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayReferrerThroughStripeResponse {
    Exception exception;
    Transfer transfer;
}
