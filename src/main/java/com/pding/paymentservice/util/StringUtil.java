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

    private static final Map<String, String> PACKAGE_TYPE_MAP = Map.of(
            "FREE_CHOICE_PACKAGE", "Free choice",
            "THEME_PACKAGE", "Theme",
            "SINGLE", "Single"
    );

    /**
     * Mask email to preserve the exact length of hidden parts.
     * Rules:
     * - Local part: keep first 2 characters; replace the remaining characters in the local part with '*'.
     * - Domain part:
     *   - If there is no dot: keep first 2 characters of the domain and replace the rest with '*'.
     *   - If there are dots: keep the last label (TLD) fully visible; for the first label, keep first 2 characters and mask the remaining with '*'; for any intermediate labels, mask all characters with '*'.
     *   - Keep the original number of dots and label lengths masked accordingly.
     * Examples:
     *   - ben@gmail.com -> be*@gm***.com
     *   - a@b.co -> a*@b*.co
     *   - john.doe@sub.mail.org -> jo******@su***.****.org
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@", 2);
        String localPart = parts[0] != null ? parts[0] : "";
        String domainPart = parts.length > 1 && parts[1] != null ? parts[1] : "";

        // Mask local part: keep first 2, mask the rest one-to-one with '*'
        int localVisible = Math.min(2, localPart.length());
        StringBuilder localMasked = new StringBuilder();
        localMasked.append(localPart, 0, localVisible);
        if (localPart.length() > localVisible) {
            localMasked.append("*".repeat(localPart.length() - localVisible));
        }

        // Mask domain part
        if (domainPart.isEmpty()) {
            return localMasked + "@"; // nothing to show after @
        }

        String[] labels = domainPart.split("\\.");
        if (labels.length == 1) {
            String label = labels[0];
            int visible = Math.min(2, label.length());
            StringBuilder maskedLabel = new StringBuilder();
            maskedLabel.append(label, 0, visible);
            if (label.length() > visible) maskedLabel.append("*".repeat(label.length() - visible));
            return localMasked + "@" + maskedLabel;
        }

        StringBuilder domainMasked = new StringBuilder();
        for (int i = 0; i < labels.length; i++) {
            String label = labels[i] == null ? "" : labels[i];
            if (i == labels.length - 1) {
                // TLD - keep fully
                domainMasked.append(label);
            } else if (i == 0) {
                int visible = Math.min(2, label.length());
                domainMasked.append(label, 0, visible);
                if (label.length() > visible) domainMasked.append("*".repeat(label.length() - visible));
            } else {
                // Intermediate label - fully masked with preserving length
                if (label.length() > 0) domainMasked.append("*".repeat(label.length()));
            }
            if (i < labels.length - 1) domainMasked.append('.');
        }

        return localMasked + "@" + domainMasked;
    }

    public static String convertDurationKeyToValue(String durationKey) {
        if (DURATION_MAP.containsKey(durationKey)) {
            return DURATION_MAP.get(durationKey);
        }
        return "";
    }

    public static String convertPackageTypeKeyToValue(String durationKey) {
        if (PACKAGE_TYPE_MAP.containsKey(durationKey)) {
            return PACKAGE_TYPE_MAP.get(durationKey);
        }
        return "";
    }
}
