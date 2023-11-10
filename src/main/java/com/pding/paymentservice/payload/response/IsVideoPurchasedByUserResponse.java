package com.pding.paymentservice.payload.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IsVideoPurchasedByUserResponse {
    ErrorResponse errorResponse;
    Boolean result;
}
