package com.pding.paymentservice.app.config.enums;

public enum LeafType {
    BASIC("BASIC"),
    SIGNATURE("SIGNATURE");

    private final String displayName;

    LeafType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
