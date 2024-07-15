package com.pding.paymentservice.payload.request.fcm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendGenericNotificationRequest {
    String userId;
    String notificationTitle;
    String notificationBody;
    Map<String, String> data;
}
