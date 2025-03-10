package com.pding.paymentservice.service;

import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.enums.VideoPurchaseDuration;
import com.pding.paymentservice.repository.VideoPurchaseRepository;
import com.pding.paymentservice.util.LogSanitizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class VideoPurchaseServiceProxy {

    @Autowired
    VideoPurchaseRepository videoPurchaseRepository;

    @Autowired
    WalletService walletService;

    @Autowired
    EarningService earningService;

    @Autowired
    LedgerService ledgerService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public VideoPurchase createVideoTransaction(String userId, String videoId, String videoOwnerUserId, BigDecimal treesToConsumed, String duration) {
        log.info("Buy video request made with following details UserId : {} ,VideoId : {}, trees : {}, VideoOwnerUserId : {}, duration : {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(videoId), LogSanitizer.sanitizeForLog(treesToConsumed), LogSanitizer.sanitizeForLog(videoOwnerUserId), LogSanitizer.sanitizeForLog(duration));
        List<VideoPurchase> videoPurchases = videoPurchaseRepository.findByUserIdAndVideoIdSelectForUpdate(userId, videoId);
        //check if video with duration not expired and already purchased
        if (videoPurchases.stream().anyMatch(vp -> vp.getExpiryDate() == null || vp.getExpiryDate().isAfter(LocalDateTime.now()))) {
            throw new IllegalArgumentException("Video already purchased");
        }

        walletService.deductTreesFromWallet(userId, treesToConsumed);

        VideoPurchase transaction = new VideoPurchase(userId, videoId, treesToConsumed, videoOwnerUserId, duration,
            VideoPurchaseDuration.valueOf(duration).getExpiryDate());

        VideoPurchase video = videoPurchaseRepository.save(transaction);

        earningService.addTreesToEarning(videoOwnerUserId, treesToConsumed);
        ledgerService.saveToLedger(video.getId(), treesToConsumed, new BigDecimal(0), TransactionType.VIDEO_PURCHASE, userId);
        log.info("Buy video request transaction completed with details UserId : {} ,VideoId : {}, trees : {}, VideoOwnerUserId : {}, duration : {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(videoId), LogSanitizer.sanitizeForLog(treesToConsumed), LogSanitizer.sanitizeForLog(videoOwnerUserId), LogSanitizer.sanitizeForLog(duration));
        return video;
    }
}
