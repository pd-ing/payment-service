package com.pding.paymentservice.payload.request;


import java.math.BigDecimal;
import lombok.Data;

@Data
public class BuyMissionRequest {
    private String buyerId;
    private String pdUserId;
    private String missionId;
    private BigDecimal treesOffered;
    private String streamId;
}
