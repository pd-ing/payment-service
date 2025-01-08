package com.pding.paymentservice.payload.request;

import lombok.Data;

@Data
public class RefundExposureTicketRequest {
    private String transactionId;
}
