package com.pding.paymentservice.service;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.aws.SendNotificationSqsMessage;
import com.pding.paymentservice.models.Donation;
import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.models.Withdrawal;
import com.pding.paymentservice.repository.NotificationRepository;
import com.pding.paymentservice.util.TokenSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    public void sendBuyVideoNotification(VideoPurchase videoPurchase) {
        try {
            String email = notificationRepository.findEmailByUserId(videoPurchase.getUserId());
            String videoTitle = notificationRepository.findTitleByVideoId(videoPurchase.getVideoId());
            String videoUrl = tokenSigner.signPlaybackUrl(libraryId, videoPurchase.getVideoId(), 2);
            sendNotificationSqsMessage.sendVideoBoughtNotification(videoPurchase.getVideoOwnerUserId(), videoPurchase.getUserId(), email, videoPurchase.getVideoOwnerUserId(), videoPurchase.getVideoId(), videoTitle, videoUrl, null);
        } catch (Exception e) {
            pdLogger.logException(PdLogger.Priority.p0, e);
        }
    }

    public void sendDonateTreesNotification(Donation donation) {
        try {
            String donorUserEmail = notificationRepository.findEmailByUserId(donation.getDonorUserId());
            sendNotificationSqsMessage.sendDonationNotification(donation.getPdUserId(), donation.getDonorUserId(), donorUserEmail, donation.getPdUserId());
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
