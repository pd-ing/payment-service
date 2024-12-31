package com.pding.paymentservice.payload.response.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paypal.sdk.models.Capture;
import com.paypal.sdk.models.SellerReceivableBreakdown;
import lombok.Data;

import java.util.List;

@Data
public class PurchaseUnit {

    @JsonProperty("reference_id")
    private String referenceId;
    private Shipping shipping;
    private Payments payments;

    @Data
    public static class Shipping {
        private Name name;
        private Address address;
    }

    @Data
    public static class Payments {
        private List<Capture> captures;

        @Data
        public static class Capture {
            private String id;
            private String status;
            private Amount amount;

            @JsonProperty("final_capture")
            private boolean finalCapture;
            @JsonProperty("seller_protection")
            private SellerProtection sellerProtection;
            @JsonProperty("seller_receivable_breakdown")
            private SellerReceivableBreakdown sellerReceivableBreakdown;
            @JsonProperty("custom_id")
            private String customId;
            private List<Link> links;
            @JsonProperty("create_time")
            private String createTime;
            @JsonProperty("update_time")
            private String updateTime;
        }
    }
}
