package com.pding.paymentservice.payload.response;

import com.stripe.model.Charge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ChargeResponse {
    ErrorResponse errorResponse;
    String charge;
}
