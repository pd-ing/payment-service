package com.pding.paymentservice.models.enums;

public enum NotificaitonDataType {

    MEDIA_PURCHASED("MEDIA_PURCHASED"),
    MEDIA_TRANSACTION_REQUEST_CHAT_ROOM("MEDIA_TRANSACTION_REQUEST_CHAT_ROOM"),
    GIFT_WEB("GIFT_WEB"),
    GIFT_RECEIVE("GIFT_RECEIVE"),
    PURCHASE_PAID_POST("PURCHASE_PAID_POST");
    private final String displayName;

    NotificaitonDataType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
