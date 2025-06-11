package com.pding.paymentservice.service;

import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.enums.VideoPurchaseDuration;
import com.pding.paymentservice.repository.VideoPurchaseRepository;
import com.pding.paymentservice.util.LogSanitizer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${video.purchase.drm.fee}")
    private BigDecimal drmFee;

    @Value("${video.purchase.4k.fee.per.minute}")
    private BigDecimal fee4kPerMinute;

    @Value("${video.purchase.advanceEncoding.fee.per.minute}")
    private BigDecimal feePremiumEncodingPerMinute;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public VideoPurchase createVideoTransaction(String userId, String videoId, String videoOwnerUserId, @NonNull Boolean drmEnable, @NonNull Boolean is4kEnable, @NonNull Boolean premiumEncodingEnable, Long videoDuration, BigDecimal treesToConsumed, String duration) {
        log.info("Buy video request made with following details UserId : {} ,VideoId : {}, trees : {}, VideoOwnerUserId : {}, duration : {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(videoId), LogSanitizer.sanitizeForLog(treesToConsumed), LogSanitizer.sanitizeForLog(videoOwnerUserId), LogSanitizer.sanitizeForLog(duration));
        List<VideoPurchase> videoPurchases = videoPurchaseRepository.findByUserIdAndVideoIdSelectForUpdate(userId, videoId);
        //check if video with duration not expired and already purchased
        if (videoPurchases.stream().anyMatch(vp -> vp.getExpiryDate() == null || vp.getExpiryDate().isAfter(LocalDateTime.now()))) {
            throw new IllegalArgumentException("Video already purchased");
        }

        walletService.deductTreesFromWallet(userId, treesToConsumed);

        // alculate premiumEncoding fee if applicable
        BigDecimal feePremiumEncoding = BigDecimal.ZERO;
        if (premiumEncodingEnable && videoDuration != null) {
            // Convert duration from seconds to minutes and round up
            long durationInMinutes = (videoDuration + 59) / 60; // Round up to the nearest minute
            feePremiumEncoding = feePremiumEncodingPerMinute.multiply(new BigDecimal(durationInMinutes));
        }

        // Calculate 4K fee if applicable
        BigDecimal fee4k = BigDecimal.ZERO;
        if (is4kEnable && videoDuration != null) {
            // Convert duration from seconds to minutes and round up
            long durationInMinutes = (videoDuration + 59) / 60; // Round up to the nearest minute
            fee4k = fee4kPerMinute.multiply(new BigDecimal(durationInMinutes));
        }

        // Total fee is DRM fee (if applicable) plus 4K fee (if applicable)
        BigDecimal totalFee = (drmEnable ? drmFee : BigDecimal.ZERO).add(fee4k).add(feePremiumEncoding);

        VideoPurchase transaction = new VideoPurchase(userId, videoId, treesToConsumed, videoOwnerUserId, duration,
            VideoPurchaseDuration.valueOf(duration).getExpiryDate(), totalFee);

        VideoPurchase video = videoPurchaseRepository.save(transaction);

        // Deduct total fee from trees earned by seller
        earningService.addTreesToEarning(videoOwnerUserId, treesToConsumed.subtract(totalFee));
        ledgerService.saveToLedger(video.getId(), treesToConsumed, new BigDecimal(0), TransactionType.VIDEO_PURCHASE, userId);
        log.info("Buy video request transaction completed with details UserId : {} ,VideoId : {}, trees : {}, VideoOwnerUserId : {}, duration : {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(videoId), LogSanitizer.sanitizeForLog(treesToConsumed), LogSanitizer.sanitizeForLog(videoOwnerUserId), LogSanitizer.sanitizeForLog(duration));
        return video;
    }
}
