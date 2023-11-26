package com.pding.paymentservice.security;

import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserRecord;
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

    public static LoggedInUserRecord fromUserRecord(UserRecord record) {
        return LoggedInUserRecord.builder()
                .uid(record.getUid())
                .email(record.getEmail())
                .phoneNumber(record.getPhoneNumber())
                .emailVerified(record.isEmailVerified())
                .disabled(record.isDisabled())
                .providerIds(Arrays.stream(record.getProviderData()).map(UserInfo::getProviderId).collect(Collectors.toList()))
                .build();

    }
}
