package com.pding.paymentservice.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PremiumEncodingFeeRequest {
    private String userId;
    private String videoId;
    private BigDecimal fee;
}
