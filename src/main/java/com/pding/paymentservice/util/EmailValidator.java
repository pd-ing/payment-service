package com.pding.paymentservice.util;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EmailValidator {

    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public boolean isValidEmail(String email) {
        try {
            if (email == null) {
                return false;
            }
            Matcher matcher = EMAIL_PATTERN.matcher(email);
            return matcher.matches();
        } catch (Exception ex) {
            return false;
        }
    }

}
