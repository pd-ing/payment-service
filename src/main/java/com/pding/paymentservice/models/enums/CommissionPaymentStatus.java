package com.pding.paymentservice.models.enums;

public enum CommissionPaymentStatus {
    PENDING("pending"),
    PAID("paid"),
    FAILED("failed");

    private final String displayName;

    CommissionPaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
