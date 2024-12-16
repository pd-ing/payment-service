package com.pding.paymentservice.security;

import com.google.firebase.auth.UserRecord;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
class PdingSecurityHolder {
    private String uid;
    private String idToken;
    private String email;
    private Boolean emailVerified;
    private UserRecord userRecord;
    private HttpServletRequest request;
}
