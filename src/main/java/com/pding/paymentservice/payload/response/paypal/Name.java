package com.pding.paymentservice.payload.response.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Name {

    @JsonProperty("given_name")
    private String givenName;
    private String surname;
    @JsonProperty("full_name")
    private String fullName;
}
