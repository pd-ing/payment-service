package com.pding.paymentservice.models.enums;

public enum TransactionType {
    TREE_PURCHASE("treePurchase"),
    DONATION("donation"),
    WITHDRAWAL_STARTED("withdrawal_started"),
    WITHDRAWAL_COMPLETED("withdrawal_completed"),
    WITHDRAWAL_FAILED("withdrawal_failed"),
    TREES_REVERTED("trees_reverted"),
    VIDEO_PURCHASE("videoPurchase");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}