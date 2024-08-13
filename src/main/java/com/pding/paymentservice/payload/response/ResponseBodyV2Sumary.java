package com.pding.paymentservice.payload.response;

import lombok.Data;

@Data
public class ResponseBodyV2Sumary {
    public String requestIdentifier;
    public String environment;
    public Long appAppleId;
    public String bundleId;
    public String productId;
}
