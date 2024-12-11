package com.pding.paymentservice.util;

import java.util.Map;

public class StringUtil {

    private static final Map<String, String> DURATION_MAP = Map.of(
            "ONE_YEAR", "1 year",
            "THREE_DAYS", "3 days",
            "THREE_MONTHS", "3 months",
            "SIX_MONTHS", "6 months",
            "SEVEN_DAYS", "7 days",
            "ONE_MONTH", "1 month",
            "PERMANENT", "Unlimited",
            "ONE_DAY", "1 day",
            "ONE_WEEK", "1 week"
    );
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];

        // Mask local part và domain part
        String maskedLocalPart = localPart.substring(0, 2) + "****" + localPart.substring(localPart.length() - 1);
        String maskedDomainPart = domainPart.substring(0, 1) + "***" + domainPart.substring(domainPart.length() - 4);

        return maskedLocalPart + "@" + maskedDomainPart;
    }

    public static String convertDurationKeyToValue(String durationKey) {
        if (DURATION_MAP.containsKey(durationKey)) {
            return DURATION_MAP.get(durationKey);
        }
        return "";
    }
}
