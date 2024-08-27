package com.pding.paymentservice.payload.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoPurchaseTimeRemainingResponse {
    private String videoId;
    private LocalDateTime expiryDate;
    private Long numberOfDaysRemaining;
    private Boolean isExpirated;
    private Boolean isPermanent;
}
