package com.pding.paymentservice.models.enums;

public enum CommissionTransferStatus {
    TRANSFER_PENDING("TRANSFER_PENDING"),
    TRANSFER_DONE("TRANSFER_DONE"),
    TRANSFER_FAILED("TRANSFER_FAILED");

    private final String displayName;

    CommissionTransferStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
