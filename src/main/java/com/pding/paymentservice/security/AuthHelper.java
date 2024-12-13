package com.pding.paymentservice.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthHelper implements FirebaseDataInfo{

    public String getUserId() {
        return getSecurityHolder()
                .map(PdingSecurityHolder::getUid)
                .orElseThrow();
    }
    public String getIdToken() throws Exception {
        return getLoggedInFirebaseUser().idToken;
    }
    @Override
    public String getUserEmail() {
        return getSecurityHolder()
                .flatMap(holder -> {
                    if (holder.getEmail() != null) {
                        return Optional.of(holder.getEmail());
                    } else {
                        return getLoggedInFirebaseUserOptional().map(LoggedInUserRecord::getEmail);
                    }
                })
                .orElseThrow();
    }
    @Override
    public Boolean isEmailVerified() throws Exception {
        return getSecurityHolder()
                .flatMap(holder -> {
                    if (holder.getEmailVerified() != null) {
                        return Optional.of(holder.getEmailVerified());
                    } else {
                        return getLoggedInFirebaseUserOptional().map(LoggedInUserRecord::getEmailVerified);
                    }
                })
                .orElseThrow();
    }

    public LoggedInUserRecord getLoggedInFirebaseUser() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof PdingSecurityHolder holder) {
                UserRecord userRecord = holder.getUserRecord();

                if(userRecord == null) {
                    userRecord =  FirebaseAuth.getInstance().getUser(holder.getUid());
                    holder.setUserRecord(userRecord);
                }

                return LoggedInUserRecord.fromUserRecord(holder.getUserRecord(), holder.getRequest());
            }
        }

        throw new Exception("Login again.");
    }

    public Optional<LoggedInUserRecord> getLoggedInFirebaseUserOptional() {
        try {
            LoggedInUserRecord userRecord = getLoggedInFirebaseUser();
            return Optional.of(userRecord);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<PdingSecurityHolder> getSecurityHolder() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof PdingSecurityHolder) {
            return Optional.of((PdingSecurityHolder) principal);
        } else {
            return Optional.empty();
        }
    }

}
