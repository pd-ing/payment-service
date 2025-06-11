package com.pding.paymentservice.error;

/**
 * Error codes for the video package purchase feature.
 * These codes are used to identify specific error messages that can be translated on the client side.
 */
public enum VideoPackagePurchaseErrorCode {
    // Package related errors
    PACKAGE_NOT_FOUND("VPP_001"),
    PACKAGE_DETAILS_ERROR("VPP_002"),

    // Package purchase validation errors
    PACKAGE_NOT_ACTIVE("VPP_003"),
    PACKAGE_SALE_NOT_STARTED("VPP_004"),
    PACKAGE_SALE_ENDED("VPP_005"),

    // Free choice package errors
    INVALID_SELECTED_VIDEOS_COUNT("VPP_006"),
    INVALID_SELECTED_VIDEOS("VPP_007"),
    VIDEOS_ALREADY_PURCHASED("VPP_008"),

    // Theme package errors
    PACKAGE_ALREADY_PURCHASED("VPP_009"),

    // Refund errors
    TRANSACTION_NOT_FOUND("VPP_010"),
    CANNOT_REFUND_PACKAGE("VPP_011");

    private final String code;

    VideoPackagePurchaseErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
