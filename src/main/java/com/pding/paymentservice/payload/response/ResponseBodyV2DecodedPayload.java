package com.pding.paymentservice.payload.response;

import lombok.Data;

@Data
public class ResponseBodyV2DecodedPayload {
    public String notificationType; //REFUND //ONE_TIME_CHARGE
    public String subtype;
    public ResponseBodyV2Data data;
    public ResponseBodyV2Sumary summary;
    public ExternalPurchaseToken externalPurchaseToken;
    public String version;
    public String notificationUUID;
}



