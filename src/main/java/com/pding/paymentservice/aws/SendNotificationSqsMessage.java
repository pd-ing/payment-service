package com.pding.paymentservice.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pding.paymentservice.BaseService;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
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
            String photoUrl,
            BigDecimal tree,
            BigDecimal drmFee
    ) throws Exception {
        if (isNotValid(sendToUserId) || isNotValid(videoBuyerUserId) || isNotValid(videoOwnerUserId) ||
                isNotValid(videoId) || isNotValid(videoTitle) || isNotValid(videoUrl)) {
            throw new Exception("Invalid params for sendVideoBoughtNotification");
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("type", "BUY_VIDEO");
        map.put("userId", sendToUserId);
        map.put("time", new Date());
        map.put("videoBuyerUserId", videoBuyerUserId);
        map.put("videoBuyerUserEmail", videoBuyerUserEmail != null ? videoBuyerUserEmail : videoBuyerUserId);
        map.put("videoOwnerUserId", videoOwnerUserId);
        map.put("videoId", videoId);
        map.put("videoTitle", videoTitle);
        map.put("videoUrl", videoUrl);
        map.put("photoUrl", photoUrl);
        map.put("tree", tree);
        map.put("drmFee", drmFee);

        sendNotification(map);
    }


    public void sendPhotoBoughtNotification(
            String sendToUserId,
            String photoBuyerUserId,
            String buyerNickname,
            String photoOwnerUserId,
            String postId,
            String postTitle,
            BigDecimal tree
    ) throws Exception {
        if (isNotValid(sendToUserId) || isNotValid(photoBuyerUserId) ||
                isNotValid(postId) || isNotValid(postTitle)) {
            throw new Exception("Invalid params for sendPhotoBoughtNotification");
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("type", "BUY_PHOTO");
        map.put("userId", sendToUserId);
        map.put("time", new Date());
        map.put("photoOwnerUserId", photoOwnerUserId);
        map.put("photoBuyerUserId", photoBuyerUserId);
        map.put("buyerNickname", buyerNickname != null ? buyerNickname : photoBuyerUserId);
        map.put("postId", postId);
        map.put("postTitle", postTitle);
        map.put("postUrl", "");
        map.put("tree", tree);

        sendNotification(map);
    }

    public void sendDonationNotification(
            String sendToUserId,
            String donnerUserId,
            String donnerUserEmail,
            String donatedToUserId,
            BigDecimal donatedTrees,
            BigDecimal donatedLeafs
    ) throws Exception {
        if (isNotValid(sendToUserId) || isNotValid(donnerUserId) || isNotValid(donatedToUserId)) {
            throw new Exception("Invalid params for sendDonationNotification");
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("type", "DONATION");
        map.put("userId", sendToUserId);
        map.put("time", new Date());
        map.put("donnerUserId", donnerUserId);
        map.put("donnerUserEmail", donnerUserEmail);
        map.put("donatedToUserId", donatedToUserId);
        map.put("donatedTrees", donatedTrees);
        map.put("donatedLeafs", donatedLeafs);

        sendNotification(map);
    }

    public void sendCurrencyExchangeProcessNotification(
            String sendToUserId,
            String currencyExchangeProgress, // started | finished
            BigDecimal trees,
            BigDecimal leafs
    ) throws Exception {
        if (isNotValid(sendToUserId) || isNotValid(currencyExchangeProgress)) {
            throw new Exception("Invalid params for sendCurrencyExchangeProcessNotification");
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("type", "CURRENCY_EXCHANGE");
        map.put("userId", sendToUserId);
        map.put("time", new Date());
        map.put("trees", trees);
        map.put("leafs", leafs);
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
            sqsTemplate.send(to -> to.queue("FCMNotificationQueue.fifo")
                            .payload(json)
                            .messageDeduplicationId(UUID.randomUUID().toString())
                            .messageGroupId(map.get("NotificationType"))
            );
        } catch (Exception ex) {
            pdLogger.logException(ex);
        }
    }

    public void sendAsyncFcmNotification(Map<String, String> map) {
        try {
            String json = objectMapper.writeValueAsString(map);
            sqsTemplate.send(to -> to.queue("FCMNotificationQueue.fifo")
                    .payload(json)
                    .messageDeduplicationId(UUID.randomUUID().toString())
                    .messageGroupId(map.get("NotificationType"))
            );
        } catch (Exception ex) {
            pdLogger.logException(ex);
        }
    }

    public void sendForceReleaseTopExposureNotification(String userId) {
        try {
            HashMap<String, Object> map = new HashMap<>();
            map.put("type", "FORCE_RELEASE_TOP_EXPOSURE");
            map.put("userId", userId);
            map.put("time", new Date());
            sendNotification(map);
        } catch (Exception ex) {
            pdLogger.logException(ex);
        }
    }

    public void sendReleaseTopExposureNotification(String userId) {
        try {
            HashMap<String, Object> map = new HashMap<>();
            map.put("type", "RELEASE_TOP_EXPOSURE");
            map.put("userId", userId);
            map.put("time", new Date());
            sendNotification(map);
        } catch (Exception ex) {
            pdLogger.logException(ex);
        }
    }

//    public boolean sendAutoExpireTopExposureSlot(String userId) {
//        try {
//            Map<String, String> map = new HashMap<>();
//            map.put("userId", userId);
//            String json = objectMapper.writeValueAsString(map);
//
//            String messageId = sqsTemplate.send(to -> to.queue("TopExposureSlotQueue")
//                .payload(json)
//                .delaySeconds(900)
//            ).messageId().toString();
//
//            return messageId != null;
//        } catch (Exception ex) {
//            log.error("Error in sendAutoExpireTopExposureSlot", ex);
//            return false;
//        }
//    }

    // is Null or Empty
    private boolean isNotValid(String a) {
        return a == null || a.isEmpty();
    }

}
