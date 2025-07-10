package com.pding.paymentservice.payload.response;

import lombok.Data;

import java.time.Instant;

@Data
public class PhotoPurchaseTimeRemainingResponse {
    private String photoPostId;
    private Instant expiryDate;
    private Long numberOfDaysRemaining;
    private Boolean isExpirated;
    private Boolean isPermanent;
}
