package com.pding.paymentservice.models.enums;

public enum TransactionType {
    TREE_PURCHASE("treePurchase"),
    LEAF_PURCHASE("leafPurchase"),
    DONATION("donation"),
    WITHDRAWAL_STARTED("withdrawal_started"),
    WITHDRAWAL_COMPLETED("withdrawal_completed"),
    WITHDRAWAL_FAILED("withdrawal_failed"),
    TREES_REVERTED("trees_reverted"),
    LEAFS_REVERTED("leafs_reverted"),
    VIDEO_PURCHASE("videoPurchase"),
    REFUND_VIDEO_PURCHASE("refundVideoPurchase"),
    IMAGE_PURCHASE("imagePurchase"),
    AUDIO_CALL("audioCall"),
    VIDEO_CALL("videoCall"),

    TEXT_MESSAGE("textMessage"),

    PAYMENT_STARTED("paymentStarted"),

    PAYMENT_COMPLETED("paymentCompleted"),

    PAYMENT_FAILED("paymentFailed"),

    REFUND_COMPLETED("refundCompleted"),

    REFUND_CANCELLED("refundCancelled"),

    ADD_TREES_FROM_BACKEND("addTreesFromBackend"),

    REMOVE_TREES_FROM_BACKEND("removeTreesFromBackend"),

    MEDIA_TRADING("mediaTrading"),

    BUY_EXPOSURE_TICKET("buyExposureTicket"),
    REFUND_EXPOSURE_TICKET("refundExposureTicket"),
    PACKAGE_PURCHASE("packagePurchase"),
    ;

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
