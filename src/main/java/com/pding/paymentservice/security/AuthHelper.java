package com.pding.paymentservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthHelper {

    public String getUserId() throws Exception {
        return getLoggedInFirebaseUser().getUid();
    }

    public String getIdToken() throws Exception {
        return getLoggedInFirebaseUser().idToken;
    }

    public String getUserEmail() throws Exception {
        return getLoggedInFirebaseUser().getEmail();
    }

    public Boolean isEmailVerified() throws Exception {
        return getLoggedInFirebaseUser().getEmailVerified();
    }

    public LoggedInUserRecord getLoggedInFirebaseUser() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof LoggedInUserRecord) {
                return (LoggedInUserRecord) principal;
            }
        }

        throw new Exception("Login again.");
    }

}
