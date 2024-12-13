package com.pding.paymentservice.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthHelper implements FirebaseDataInfo{

    public String getUserId() {
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if(principal instanceof PdingSecurityHolder holder){
            return holder.getUid();
        }else{
            return null;
        }
    }
    public String getIdToken() throws Exception {
        return getLoggedInFirebaseUser().idToken;
    }
    @Override
    public String getUserEmail() throws Exception {
        return getLoggedInFirebaseUser().getEmail();
    }
    @Override
    public Boolean isEmailVerified() throws Exception {
        return getLoggedInFirebaseUser().getEmailVerified();
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

}
