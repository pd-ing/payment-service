package com.pding.paymentservice.payload.response;

import lombok.Data;

@Data
public class ExternalPurchaseToken {
    public String externalPurchaseId;
    public Integer tokenCreationDate;
    public Long appAppleId;
    public String bundleId;
}
