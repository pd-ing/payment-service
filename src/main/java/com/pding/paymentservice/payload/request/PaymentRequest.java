package com.pding.paymentservice.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PaymentRequest {
    private String intent;

    @JsonProperty("purchase_units")
    private List<PurchaseUnit> purchaseUnits;

    @Data
    public static class PurchaseUnit {
        private Amount amount;

        @JsonProperty("custom_id")
        private String customId;
    }

    @Data
    public static class Amount {
        @JsonProperty("currency_code")
        private String currencyCode;
        private BigDecimal value;
    }
}
