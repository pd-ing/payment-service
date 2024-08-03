package com.pding.paymentservice.service;

import com.pding.paymentservice.BaseService;
import com.pding.paymentservice.aws.SendNotificationSqsMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FcmService extends BaseService {

    @Autowired
    SendNotificationSqsMessage sendNotificationSqsMessage;

    public String sendNotification(String userId, Map<String, String> data) {
        data.put("toUserId", userId);
        sendNotificationSqsMessage.sendFcmNotification(data);
        return "Successfully sent message";
    }
}
