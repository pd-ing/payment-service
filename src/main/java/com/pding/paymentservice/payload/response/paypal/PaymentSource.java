package com.pding.paymentservice.payload.response.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaymentSource {
    private PayPal paypal;

    @Data
    public static class PayPal {
        @JsonProperty("email_address")
        private String emailAddress;
        @JsonProperty("account_id")
        private String accountId;
        @JsonProperty("account_status")
        private String accountStatus;
        private Name name;
        private Address address;
    }
}
