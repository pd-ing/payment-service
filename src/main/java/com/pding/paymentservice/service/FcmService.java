package com.pding.paymentservice.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.pding.paymentservice.models.DeviceToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.Message;

import java.util.List;
import java.util.Map;

@Service
public class FcmService {
    @Autowired
    private DeviceTokenService deviceTokenService;

    public String sendNotification(String userId, Map<String, String> data) throws Exception {
        List<DeviceToken> tokens = deviceTokenService.getTokensByUserId(userId);

        if (tokens.isEmpty())
            return "No token found for the provided userId";

        for (DeviceToken deviceToken : tokens) {
//            Notification notification = Notification.builder()
//                    .setTitle(title)
//                    .setBody(body)
//                    .build();

            Message message = Message.builder()
                    .setToken(deviceToken.getToken())
                    //                   .setNotification(notification)
                    .putAllData(data)
                    .build();


            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent message: " + response);
        }

        return "Successfully sent message";
    }
}
