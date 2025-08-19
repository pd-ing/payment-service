package com.pding.paymentservice.payload.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BuyLivestreamRequest {
    private String pdUserId;
    private String livestreamId;
    private BigDecimal treesOffered;
}