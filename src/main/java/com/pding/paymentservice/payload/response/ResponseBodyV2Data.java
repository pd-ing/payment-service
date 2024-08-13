package com.pding.paymentservice.payload.response;

import lombok.Data;

@Data
public class ResponseBodyV2Data {
    public Long appAppleId;
    public String bundleId;
    public String bundleVersion;
    public String consumptionRequestReason;
    public String environment;
    public String signedRenewalInfo;
    public String signedTransactionInfo;
    public Integer status;
}
