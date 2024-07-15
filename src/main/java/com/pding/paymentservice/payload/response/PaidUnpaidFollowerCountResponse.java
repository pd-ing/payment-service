package com.pding.paymentservice.payload.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaidUnpaidFollowerCountResponse {
    ErrorResponse errorResponse;
    BigInteger paidFollowerCount;
    BigInteger unpaidFollowerCount;
}
