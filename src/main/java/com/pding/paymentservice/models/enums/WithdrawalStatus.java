package com.pding.paymentservice.models.enums;

public enum WithdrawalStatus {
    PENDING("pending"),
    COMPLETE("complete"),
    FAILED("failed");

    private final String displayName;

    WithdrawalStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
