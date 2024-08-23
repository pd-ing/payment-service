package com.pding.paymentservice.models.enums;

import java.time.LocalDateTime;

public enum VideoPurchaseDuration {
    THREE_DAYS("THREE_DAYS"),
    SEVEN_DAYS("SEVEN_DAYS"),
    ONE_MONTH("ONE_MONTH"),
    THREE_MONTHS("THREE_MONTHS"),
    SIX_MONTHS("SIX_MONTHS"),
    ONE_YEAR("ONE_YEAR"),
    PERMANENT("PERMANENT");

    private final String value;

    VideoPurchaseDuration(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public LocalDateTime getExpiryDate() {
        switch (this) {
            case THREE_DAYS:
                return LocalDateTime.now().plusDays(3);
            case SEVEN_DAYS:
                return LocalDateTime.now().plusDays(7);
            case ONE_MONTH:
                return LocalDateTime.now().plusMonths(1);
            case THREE_MONTHS:
                return LocalDateTime.now().plusMonths(3);
            case SIX_MONTHS:
                return LocalDateTime.now().plusMonths(6);
            case ONE_YEAR:
                return LocalDateTime.now().plusYears(1);
            case PERMANENT:
                return LocalDateTime.now().plusYears(100);
            default:
                throw new IllegalArgumentException("Invalid duration: " + this.value);
        }
    }
}
