package com.pding.paymentservice.security;

public interface FirebaseDataInfo {
    String getUserEmail() throws Exception;

    Boolean isEmailVerified() throws Exception;
}
