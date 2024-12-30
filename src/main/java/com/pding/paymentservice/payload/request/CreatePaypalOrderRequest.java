package com.pding.paymentservice.payload.request;

import lombok.Data;

@Data
public class CreatePaypalOrderRequest {
    private String priceId;
}
