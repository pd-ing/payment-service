package com.pding.paymentservice.payload.request;

import lombok.Data;


import java.util.Set;

@Data
public class VideoPurchaseTimeRemainingRequest {
    private String userId;
    private Set<String> videoIds;
}
