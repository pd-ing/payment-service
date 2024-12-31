package com.pding.paymentservice.payload.response.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paypal.sdk.models.Payer;
import lombok.Data;

import java.util.List;

@Data
public class PayPalCaptureOrder {
    private String id;
    private String status;
    @JsonProperty("payment_source")
    private PaymentSource paymentSource;

    @JsonProperty("purchase_units")
    private List<PurchaseUnit> purchaseUnits;

    private Payer payer;
    private List<Link> links;
}
