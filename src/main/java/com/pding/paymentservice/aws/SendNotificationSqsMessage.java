package com.pding.paymentservice.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pding.paymentservice.BaseService;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class SendNotificationSqsMessage extends BaseService {

    @Autowired
    private SqsTemplate sqsTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    public void sendVideoBoughtNotification(
            String sendToUserId,
            String videoBuyerUserId,
            String videoBuyerUserEmail,
            String videoOwnerUserId,
            String videoId,
            String videoTitle,
            String videoUrl,
            String photoUrl //url of thumbnail
    ) throws Exception {
        if (isNotValid(sendToUserId) || isNotValid(videoBuyerUserId) || isNotValid(videoBuyerUserEmail) || isNotValid(videoOwnerUserId) ||
                isNotValid(videoId) || isNotValid(videoTitle) || isNotValid(videoUrl)) {
            throw new Exception("Invalid params for sendVideoBoughtNotification");
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("type", "BUY_VIDEO");
        map.put("userId", sendToUserId);
        map.put("time", new Date());
        map.put("videoBuyerUserId", videoBuyerUserId);
        map.put("videoBuyerUserEmail", videoBuyerUserEmail);
        map.put("videoOwnerUserId", videoOwnerUserId);
        map.put("videoId", videoId);
        map.put("videoTitle", videoTitle);
        map.put("videoUrl", videoUrl);
        map.put("photoUrl", photoUrl);

        sendNotification(map);
    }

    public void sendDonationNotification(
            String sendToUserId,
            String donnerUserId,
            String donnerUserEmail,
            String donatedToUserId
    ) throws Exception {
        if (isNotValid(sendToUserId) || isNotValid(donnerUserId) || isNotValid(donnerUserEmail) || isNotValid(donatedToUserId)) {
            throw new Exception("Invalid params for sendDonationNotification");
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("type", "DONATION");
        map.put("userId", sendToUserId);
        map.put("time", new Date());
        map.put("donnerUserId", donnerUserId);
        map.put("donnerUserEmail", donnerUserEmail);
        map.put("donatedToUserId", donatedToUserId);

        sendNotification(map);
    }

    public void sendCurrencyExchangeProcessNotification(
            String sendToUserId,
            String currencyExchangeProgress // started | finished
    ) throws Exception {
        if (isNotValid(sendToUserId) || isNotValid(currencyExchangeProgress)) {
            throw new Exception("Invalid params for sendCurrencyExchangeProcessNotification");
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("type", "CURRENCY_EXCHANGE");
        map.put("userId", sendToUserId);
        map.put("time", new Date());
        map.put("currencyExchangeProgress", currencyExchangeProgress);

        sendNotification(map);
    }

    public void sendNotification(HashMap<String, Object> map) {
        try {
            String json = objectMapper.writeValueAsString(map);
            sqsTemplate.send("NotificationQueue.fifo", json);
        } catch (Exception ex) {
            pdLogger.logException(ex);
        }
    }


    public void sendFcmNotification(Map<String, String> map) {
        try {
            String json = objectMapper.writeValueAsString(map);
            sqsTemplate.send("FCMNotificationQueue", json);
        } catch (Exception ex) {
            pdLogger.logException(ex);
        }
    }

    // is Null or Empty
    private boolean isNotValid(String a) {
        return a == null || a.isEmpty();
    }

}
