package com.pding.paymentservice.util;

public class StringUtil {
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
}
