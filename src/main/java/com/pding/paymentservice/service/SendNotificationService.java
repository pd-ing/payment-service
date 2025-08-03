package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.aws.SendNotificationSqsMessage;
import com.pding.paymentservice.models.Donation;
import com.pding.paymentservice.models.PhotoPurchase;
import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.models.Withdrawal;
import com.pding.paymentservice.models.enums.NotificaitonDataType;
import com.pding.paymentservice.repository.NotificationRepository;
import com.pding.paymentservice.repository.OtherServicesTablesNativeQueryRepository;
import com.pding.paymentservice.util.TokenSigner;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    PdLogger pdLogger;

    @Autowired
    FcmService fcmService;

    @Autowired
    OtherServicesTablesNativeQueryRepository otherServicesTablesNativeQueryRepository;

    public void sendBuyVideoNotification(VideoPurchase videoPurchase, String videoLibraryId) {
        try {
            String buyerNickname = notificationRepository.findNicknameByUserId(videoPurchase.getUserId());
            String videoTitle = notificationRepository.findTitleByVideoId(videoPurchase.getVideoId());
            String videoUrl = tokenSigner.signPlaybackUrl(videoLibraryId, videoPurchase.getVideoId(), 2);
            sendNotificationSqsMessage.sendVideoBoughtNotification(videoPurchase.getVideoOwnerUserId(),
                videoPurchase.getUserId(),
                buyerNickname,
                videoPurchase.getVideoOwnerUserId(),
                videoPurchase.getVideoId(),
                videoTitle,
                videoUrl,
                null,
                videoPurchase.getTreesConsumed(),
                videoPurchase.getDrmFee()
            );

            //push FCM
            Map<String, String> data = new HashMap<>();
            data.put("NotificationType", NotificaitonDataType.PURCHASE_PAID_POST.getDisplayName());
            data.put("nickname", buyerNickname);
            data.put("numberOfTree", String.valueOf(videoPurchase.getTreesConsumed()));
            data.put("videoId", String.valueOf(videoPurchase.getVideoId()));
            data.put("postId", String.valueOf(videoPurchase.getVideoId()));
            data.put("videoUrl", videoUrl);
            data.put("videoTitle", videoTitle);
            fcmService.sendAsyncNotification(videoPurchase.getVideoOwnerUserId(), data);
        } catch (Exception e) {
            pdLogger.logException(PdLogger.Priority.p0, e);
        }
    }


    public void sendBuyPhotoNotification(PhotoPurchase photoPurchase, String postTitle) {
        try {
            String buyerNickname = notificationRepository.findNicknameByUserId(photoPurchase.getUserId());
            sendNotificationSqsMessage.sendPhotoBoughtNotification(
                photoPurchase.getPostOwnerUserId(),
                photoPurchase.getUserId(),
                buyerNickname,
                photoPurchase.getPostOwnerUserId(),
                photoPurchase.getPostId(),
                postTitle,
                photoPurchase.getTreesConsumed()
            );

        } catch (Exception e) {
            pdLogger.logException(PdLogger.Priority.p0, e);
        }
    }

    public void sendDonateTreesNotification(Donation donation) {
        try {
            String donorUserEmail = notificationRepository.findNicknameByUserId(donation.getDonorUserId());
            sendNotificationSqsMessage.sendDonationNotification(donation.getPdUserId(), donation.getDonorUserId(), donorUserEmail, donation.getPdUserId(), donation.getDonatedTrees(), donation.getDonatedLeafs());

            //push FCM
            Map<String, String> data = new HashMap<>();
            data.put("userId", donation.getDonorUserId());
            data.put("NotificationType", NotificaitonDataType.GIFT_WEB.getDisplayName());
            data.put("numberOfTree", String.valueOf(donation.getDonatedTrees()));
            data.put("nickname", otherServicesTablesNativeQueryRepository.getNicknameByUserId(donation.getDonorUserId()).orElse("User"));
            fcmService.sendAsyncNotification(donation.getPdUserId(), data );
        } catch (Exception e) {
            pdLogger.logException(PdLogger.Priority.p0, e);
        }
    }

    public void sendWithDrawNotification(Withdrawal withdrawal) {
        try {
            sendNotificationSqsMessage.sendCurrencyExchangeProcessNotification(withdrawal.getPdUserId(), withdrawal.getStatus().getDisplayName(), withdrawal.getTrees(), withdrawal.getLeafs());
        } catch (Exception e) {
            pdLogger.logException(PdLogger.Priority.p0, e);
        }
    }
}
