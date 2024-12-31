package com.pding.paymentservice.payload.response.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Payer {
    private Name name;
    @JsonProperty("email_address")
    private String emailAddress;
    @JsonProperty("payer_id")
    private String payerId;

    private Address address;
}
