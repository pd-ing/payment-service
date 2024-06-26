package com.pding.paymentservice.payload.request.fcm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendNotificationRequest {
    String userId;
    String giftId;
}
