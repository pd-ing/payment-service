package com.pding.paymentservice.models.enums;

public enum NotificaitonDataType {

    MEDIA_PURCHASED("MEDIA_PURCHASED"),
    GIFT_RECEIVE("GIFT_RECEIVE");
    private final String displayName;

    NotificaitonDataType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
