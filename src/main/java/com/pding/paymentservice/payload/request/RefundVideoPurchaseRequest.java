package com.pding.paymentservice.payload.request;

import lombok.Data;

@Data
public class RefundVideoPurchaseRequest {
    private String transactionId;
}
