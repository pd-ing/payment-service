package com.pding.paymentservice.payload.response.paypal;

import lombok.Data;

@Data
public class Amount {
    private String currencyCode;
    private String value;
}
