package com.pding.paymentservice.listener;

import com.pding.paymentservice.listener.event.VideoPackagePurchaseUpdatedEvent;
import com.pding.paymentservice.models.VideoPackagePurchase;
import com.pding.paymentservice.network.ContentNetworkService;
import com.pding.paymentservice.repository.VideoPackagePurchaseRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class VideoPackagePurchaseListener {
    private final VideoPackagePurchaseRepository videoPackagePurchaseRepository;
    private final ContentNetworkService contentNetworkService;

    @TransactionalEventListener
    @Async
    @Transactional(readOnly = true)
    public void handleVideoPackagePurchaseUpdatedEvent(VideoPackagePurchaseUpdatedEvent event) {
        log.info("Received VideoPackagePurchaseUpdatedEvent for packageId: {}", event.getPackageId());
        List<VideoPackagePurchase> purchases = videoPackagePurchaseRepository.findByPackageIdAndIsRefundedFalseForUpdate(event.getPackageId());
        // Calculate quantity sold
        int quantitySold = purchases.size();

        // Calculate total trees earned
        BigDecimal totalTreesEarned = purchases.stream()
                .map(VideoPackagePurchase::getTreesConsumed)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDrmFee = purchases.stream()
                .map(VideoPackagePurchase::getDrmFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        contentNetworkService.saveVideoPackageSalesStats(event.getPackageId(), quantitySold, totalTreesEarned.subtract(totalDrmFee));
    }
}
