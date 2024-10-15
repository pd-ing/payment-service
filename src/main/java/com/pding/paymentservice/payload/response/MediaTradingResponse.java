package com.pding.paymentservice.payload.response;

import lombok.Data;

@Data
public class MediaTradingResponse {
    private String userId;
    private String pdId;
    private String messageId;
    private String leafsToCharge;
    private String type;
    private String imageUrl;
    private String assetUrl;
    private String thumbUrl;
    private String transactionStatus;
}
