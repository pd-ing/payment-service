package com.pding.paymentservice.payload.notification;

import lombok.Data;

@Data
public class VoidedPurchaseNotification {
    private String purchaseToken;
    private String orderId;

    private Integer productType;    // 1- PRODUCT_TYPE_SUBSCRIPTION - A subscription purchase has been voided.
                                    // 2- PRODUCT_TYPE_ONE_TIME - A one-time purchase has been voided.
    private Integer refundType;     // 1- REFUND_TYPE_FULL_REFUND - The purchase has been fully voided.
                                    // 2- REFUND_TYPE_QUANTITY_BASED_PARTIAL_REFUND - The purchase has been partially voided by a quantity-based partial refund,
                                    // applicable only to multi-quantity purchases. A purchase can be partially voided multiple times.
}
