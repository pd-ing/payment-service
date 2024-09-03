package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.aws.SendNotificationSqsMessage;
import com.pding.paymentservice.models.Donation;
import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.models.Withdrawal;
import com.pding.paymentservice.models.enums.NotificaitonDataType;
import com.pding.paymentservice.repository.NotificationRepository;
import com.pding.paymentservice.repository.OtherServicesTablesNativeQueryRepository;
import com.pding.paymentservice.util.TokenSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SendNotificationService {

    @Autowired
    SendNotificationSqsMessage sendNotificationSqsMessage;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    TokenSigner tokenSigner;

    @Value("${bunny.libraryId}")
    private String libraryId;

    @Autowired
    PdLogger pdLogger;

    @Autowired
    FcmService fcmService;

    @Autowired
    OtherServicesTablesNativeQueryRepository otherServicesTablesNativeQueryRepository;

    public void sendBuyVideoNotification(VideoPurchase videoPurchase) {
        try {
            String email = notificationRepository.findEmailByUserId(videoPurchase.getUserId());
            String videoTitle = notificationRepository.findTitleByVideoId(videoPurchase.getVideoId());
            String videoUrl = tokenSigner.signPlaybackUrl(libraryId, videoPurchase.getVideoId(), 2);
            sendNotificationSqsMessage.sendVideoBoughtNotification(videoPurchase.getVideoOwnerUserId(), videoPurchase.getUserId(), email, videoPurchase.getVideoOwnerUserId(), videoPurchase.getVideoId(), videoTitle, videoUrl, null);

            //push FCM
            Map<String, String> data = new HashMap<>();
            data.put("NotificationType", NotificaitonDataType.PURCHASE_PAID_POST.getDisplayName());
            data.put("buyerNickname", otherServicesTablesNativeQueryRepository.getNicknameByUserId(videoPurchase.getUserId()).orElse("User"));
            data.put("numberOfTree", String.valueOf(videoPurchase.getTreesConsumed()));
            data.put("videoId", String.valueOf(videoPurchase.getVideoId()));
            data.put("videoUrl", videoUrl);
            data.put("videoTitle", videoTitle);
        } catch (Exception e) {
            pdLogger.logException(PdLogger.Priority.p0, e);
        }
    }

    public void sendDonateTreesNotification(Donation donation) {
        try {
            String donorUserEmail = notificationRepository.findEmailByUserId(donation.getDonorUserId());
            sendNotificationSqsMessage.sendDonationNotification(donation.getPdUserId(), donation.getDonorUserId(), donorUserEmail, donation.getPdUserId());

            //push FCM
            Map<String, String> data = new HashMap<>();
            data.put("NotificationType", NotificaitonDataType.GIFT_WEB.getDisplayName());
            data.put("numberOfTree", String.valueOf(donation.getDonatedTrees()));
            data.put("donorNickname", otherServicesTablesNativeQueryRepository.getNicknameByUserId(donation.getDonorUserId()).orElse("User"));
            fcmService.sendAsyncNotification(donation.getPdUserId(), data );
        } catch (Exception e) {
            pdLogger.logException(PdLogger.Priority.p0, e);
        }
    }

    public void sendWithDrawNotification(Withdrawal withdrawal) {
        try {
            sendNotificationSqsMessage.sendCurrencyExchangeProcessNotification(withdrawal.getPdUserId(), withdrawal.getStatus().getDisplayName());
        } catch (Exception e) {
            pdLogger.logException(PdLogger.Priority.p0, e);
        }
    }
}
