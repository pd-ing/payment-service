package com.pding.paymentservice.security;

import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserRecord;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoggedInUserRecord {

    String uid;

    String email;

    String phoneNumber;

    Boolean emailVerified;

    Boolean disabled;

    List<String> providerIds;

    String idToken;

    String httpApiEndpoint;
    String httpMethod;
    String httpRemoteAddr;
    String httpUserAgent;
    String platform;
    String clientVersion;

    public static LoggedInUserRecord fromUserRecord(UserRecord record, HttpServletRequest request) {
        return LoggedInUserRecord.builder()
                .uid(record.getUid())
                .email(record.getEmail())
                .phoneNumber(record.getPhoneNumber())
                .emailVerified(record.isEmailVerified())
                .disabled(record.isDisabled())
                .providerIds(Arrays.stream(record.getProviderData()).map(UserInfo::getProviderId).collect(Collectors.toList()))
                .httpApiEndpoint(request.getRequestURI())
                .httpMethod(request.getMethod())
                .httpRemoteAddr(request.getRemoteAddr())
                .httpUserAgent(request.getHeader("User-Agent"))
                .platform(request.getHeader("PDing-Platform"))
                .clientVersion(request.getHeader("PDing-ClientVersion"))
                .build();

    }
}
