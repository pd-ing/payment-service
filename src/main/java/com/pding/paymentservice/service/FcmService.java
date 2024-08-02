package com.pding.paymentservice.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.DeviceToken;
import com.pding.paymentservice.repository.OtherServicesTablesNativeQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Service
public class FcmService {
    @Autowired
    private DeviceTokenService deviceTokenService;

    @Autowired
    OtherServicesTablesNativeQueryRepository otherServicesTablesNativeQueryRepository;

    @Autowired
    PdLogger pdLogger;

    public String sendNotification(String userId, Map<String, String> data) throws Exception {
        List<DeviceToken> tokens = deviceTokenService.getTokensByUserId(userId);

        if (tokens.isEmpty())
            return "No token found for the provided userId";

        Notification notification = Notification.builder()
                .setTitle("Notification :" + data.get("NotificationType"))
                .setBody("leafsTransacted :" + data.get("leafsTransacted"))
                .build();

        List<String> registrationTokens = tokens.stream().map(DeviceToken::getToken).collect(Collectors.toList());
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(notification)
                .putAllData(data)
                .addAllTokens(registrationTokens)
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);

            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                List<String> failedTokens = new ArrayList<>();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        // The order of responses corresponds to the order of the registration tokens.
                        failedTokens.add(registrationTokens.get(i));
                        pdLogger.logInfo("SEND_FCM", "FCM message sent fail, deviceId: " + registrationTokens.get(i));
                    }
                    pdLogger.logInfo("SEND_FCM", "FCM message sent successfully, deviceId: " + registrationTokens.get(i));
                }
            } else {
                pdLogger.logInfo("SEND_FCM", "FCM message sent successfully to userId: " + userId);
            }
        } catch (Exception e) {
            pdLogger.logException(e);
        }

        return "Successfully sent message";
    }

    public String sendGenericNotification(String userId, Map<String, String> data) throws Exception {
        List<DeviceToken> tokens = deviceTokenService.getTokensByUserId(userId);

        if (tokens.isEmpty())
            return "No token found for the provided userId";


        String notificationType = data.get("NotificationType");
        if (notificationType == null || notificationType.isEmpty()) {
            throw new IllegalArgumentException("NotificationType is required in data map");
        }

        String titleKey = "notification." + notificationType + ".title";
        String bodyKey = "notification." + notificationType + ".body";

        String languageCode = getLanguageCode(userId);
        String title = getLocalizedString(titleKey, languageCode);
        String body = getLocalizedString(bodyKey, languageCode);

        for (DeviceToken deviceToken : tokens) {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(deviceToken.getToken())
                    .setNotification(notification)
                    .putAllData(data)
                    .build();


            String response = "";
            try {
                response = FirebaseMessaging.getInstance().send(message);
                pdLogger.logInfo("SEND_FCM", "FCM message sent successfully to the userId " + deviceToken.getUserId() + " , FCMToken : " + deviceToken.getToken() + " , DeviceId : " + deviceToken.getDeviceId());
            } catch (Exception e) {
                pdLogger.logException(e);
            }

            System.out.println("Successfully sent message: " + response);
        }
        return "Successfully sent message";
    }

    public String getLocalizedString(String key, String languageCode) {
        Locale locale = new Locale(languageCode);
        ResourceBundle bundle = ResourceBundle.getBundle("lang.messages", locale);
        return bundle.getString(key);
    }

    private String getLanguageCode(String userId) {
        Optional<String> languageByIdOptional = otherServicesTablesNativeQueryRepository.findLanguageById(userId);
        return languageByIdOptional.orElse("en");
    }
}
