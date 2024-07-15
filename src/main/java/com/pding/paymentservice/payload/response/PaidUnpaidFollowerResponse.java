package com.pding.paymentservice.payload.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaidUnpaidFollowerResponse {
    ErrorResponse errorResponse;
    String paidFollowers;
    String unpaidFollowers;
}
