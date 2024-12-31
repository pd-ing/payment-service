package com.pding.paymentservice.payload.response.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SellerProtection {
    private String status;
    @JsonProperty("dispute_categories")
    private List<String> disputeCategories;
}
