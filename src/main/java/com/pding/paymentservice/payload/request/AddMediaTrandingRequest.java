package com.pding.paymentservice.payload.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddMediaTrandingRequest {
    private String userId;
    private String pdId;
    private String messageId;
    private BigDecimal leafsToCharge;
    private String attachments;
    private String mediaType;
    private Boolean isCancel;
    private String transactionStatus;
    private String cid;
}
