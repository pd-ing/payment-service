package com.pding.paymentservice.models;

public enum TransactionType {
    TREE_PURCHASE("treePurchase"),
    DONATION("donation"),
    WITHDRAWAL("withdrawal"),
    VIDEO_PURCHASE("videoPurchase");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}