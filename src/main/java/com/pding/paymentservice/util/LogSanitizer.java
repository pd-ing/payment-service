package com.pding.paymentservice.util;

import java.math.BigDecimal;

public class LogSanitizer {
    public static String sanitizeForLog(Object input) {
        if(input == null) {
            return null;
        }
        return input.toString().replaceAll("[\r\n]", "_");
    }
}
