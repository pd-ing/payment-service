package com.pding.paymentservice.service;

import com.pding.paymentservice.models.VideoPackagePurchase;
import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.enums.VideoPurchaseDuration;
import com.pding.paymentservice.network.ContentNetworkService;
import com.pding.paymentservice.payload.net.VideoPackageDetailsResponseNet;
import com.pding.paymentservice.payload.net.VideoPackageItemDTONet;
import com.pding.paymentservice.payload.request.PurchaseVideoPackageRequest;
import com.pding.paymentservice.payload.response.PurchaseVideoPackageResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import com.pding.paymentservice.repository.VideoPackagePurchaseRepository;
import com.pding.paymentservice.repository.VideoPurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for handling video package purchases
 */
@Service
@RequiredArgsConstructor
public class VideoPackagePurchaseService {
    private final VideoPackagePurchaseRepository videoPackagePurchaseRepository;
    private final VideoPurchaseRepository videoPurchaseRepository;
    private final WalletService walletService;
    private final EarningService earningService;
    private final ContentNetworkService contentNetworkService;
    private final LedgerService ledgerService;

    /**
     * Purchase a video package
     *
     * @param userId  The user ID
     * @param request The purchase request
     * @return Response with purchase details or error
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> purchaseVideoPackage(String userId, PurchaseVideoPackageRequest request) {
        Optional<VideoPackagePurchase> packagePurchased = videoPackagePurchaseRepository.findUnrefundedPackageByUserIdAndPackageIdForUpdate(userId, request.getPackageId());
        if (packagePurchased.isPresent()) {
            throw new IllegalStateException("Package already purchased");
        }

        String packageId = request.getPackageId();
        VideoPackageDetailsResponseNet packageDetail = contentNetworkService.getPackageDetails(userId, packageId)
            .blockOptional()
            .orElseThrow(() -> new NoSuchElementException("Package not found or error getting package details"));

        if (!packageDetail.getIsActive()) {
            throw new IllegalStateException("Package sale is not active");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(packageDetail.getStartDate())) {
            throw new IllegalStateException("Package sale has not started yet");
        }
        if (now.isAfter(packageDetail.getEndDate())) {
            throw new IllegalStateException("Package sale has ended");
        }

        List<VideoPackageItemDTONet> items = packageDetail.getItems();
        Integer discountPercentage = packageDetail.getDiscountPercentage();
        String sellerId = packageDetail.getSellerId();

        if (userId.equalsIgnoreCase(sellerId)) {
            throw new IllegalStateException("Can not purchase package because sellerId is the same");
        }

        Map<String, BigDecimal> videoPrices = items.stream()
            .collect(Collectors.toMap(
                VideoPackageItemDTONet::getVideoId,
                VideoPackageItemDTONet::getPermanentPrice
            ));

        Set<String> includedVideoIds = new HashSet<>();
        Set<String> ownedVideoIds = new HashSet<>();
        BigDecimal personalizedTotalPrice = calculatePersonalizedPrice(userId, items, videoPrices, discountPercentage, includedVideoIds, ownedVideoIds);

        walletService.deductTreesFromWallet(userId, personalizedTotalPrice);
        earningService.addTreesToEarning(sellerId, personalizedTotalPrice);
        ledgerService.saveToLedger(packageId, personalizedTotalPrice, new BigDecimal(0), TransactionType.PACKAGE_PURCHASE, userId);

        VideoPackagePurchase packagePurchase = new VideoPackagePurchase(
            userId,
            packageId,
            sellerId,
            personalizedTotalPrice,
            includedVideoIds,
            ownedVideoIds,
            items.stream().map(VideoPackageItemDTONet::getPermanentPrice).reduce(BigDecimal.ZERO, BigDecimal::add),
            discountPercentage
        );
        videoPackagePurchaseRepository.save(packagePurchase);

        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
            new BigDecimal(discountPercentage)
                .divide(new BigDecimal(100))
        );

        List<VideoPurchase> videoPurchases = includedVideoIds.stream().map(videoId -> {
                BigDecimal videoPrice = videoPrices.get(videoId);
                String duration = "PERMANENT";
                LocalDateTime expiryDate = VideoPurchaseDuration.valueOf(duration).getExpiryDate();
                return new VideoPurchase(
                    userId,
                    videoId,
                    videoPrice.multiply(discountMultiplier).setScale(0, BigDecimal.ROUND_DOWN),
                    sellerId,
                    duration,
                    expiryDate,
                    discountPercentage,
                        packagePurchase.getId()
                        );
            }
        ).collect(Collectors.toList());
        videoPurchaseRepository.saveAll(videoPurchases);

        PurchaseVideoPackageResponse response = PurchaseVideoPackageResponse.builder()
            .packagePurchaseId(packagePurchase.getId())
            .packageId(packageId)
            .userId(userId)
            .sellerId(sellerId)
            .treesConsumed(personalizedTotalPrice)
            .purchaseDate(LocalDateTime.now())
            .includedVideoIds(includedVideoIds)
            .excludedVideoIds(ownedVideoIds)
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private BigDecimal calculatePersonalizedPrice(String userId, List<VideoPackageItemDTONet> items, Map<String, BigDecimal> videoPrices, Integer discountPercentage, Set<String> includedVideoIds, Set<String> ownedVideoIds) {
        Set<String> videoIds = items.stream()
            .map(VideoPackageItemDTONet::getVideoId)
            .collect(Collectors.toSet());

        List<VideoPurchase> purchasedVideos = videoPurchaseRepository
            .getPermanentVideoPurchasesByUserIdAndVideoId(userId, videoIds);

        ownedVideoIds.addAll(purchasedVideos.stream()
            .map(VideoPurchase::getVideoId)
            .collect(Collectors.toSet()));

        includedVideoIds.addAll(videoIds.stream()
            .filter(id -> !ownedVideoIds.contains(id))
            .collect(Collectors.toSet()));

        BigDecimal subtotal = includedVideoIds.stream()
            .map(videoPrices::get)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
            new BigDecimal(discountPercentage)
                .divide(new BigDecimal(100))
        );

        return subtotal.multiply(discountMultiplier).setScale(0, BigDecimal.ROUND_DOWN);
    }

    /**
     * Get all package purchases by user ID
     *
     * @param userId The user ID
     * @return List of package purchases
     */
    public ResponseEntity<?> getPackagePurchasesByUserId(String userId) {
        List<VideoPackagePurchase> purchases = videoPackagePurchaseRepository.findByUserId(userId);
        return ResponseEntity.ok(purchases);
    }

    /**
     * Get all package purchases by seller ID
     *
     * @param sellerId The seller ID
     * @return List of package purchases
     */
    public ResponseEntity<?> getPackagePurchasesBySellerId(String sellerId) {
        List<VideoPackagePurchase> purchases = videoPackagePurchaseRepository.findBySellerId(sellerId);
        return ResponseEntity.ok(purchases);
    }

    /**
     * Check if a user has purchased a specific package
     *
     * @param userId    The user ID
     * @param packageId The package ID
     * @return True if the user has purchased the package
     */
    public boolean hasUserPurchasedPackage(String userId, String packageId) {
        return videoPackagePurchaseRepository.existsByUserIdAndPackageIdAndIsRefundedFalse(userId, packageId);
    }

    public ResponseEntity<?> checkPurchaseVideoPackage(String buyerId, Set<String> packageIds) {
        List<VideoPackagePurchase> purchases = videoPackagePurchaseRepository.findAllByUserIdAndPackageIdInAndIsRefundedFalse(buyerId, packageIds);
        Set<String> purchasedPackageIds = purchases.stream().map(VideoPackagePurchase::getPackageId).collect(Collectors.toSet());
        Map<String, Boolean> mapPackageIdIsPurchased = new HashMap<>();
        for (String packageId : packageIds) {
            mapPackageIdIsPurchased.put(packageId, purchasedPackageIds.contains(packageId));
        }
        return ResponseEntity.ok(mapPackageIdIsPurchased);
    }

    @Transactional
    public ResponseEntity<?> refundPackagePurchase(String transactionId) {
        VideoPackagePurchase videoPackagePurchaseToRefund = videoPackagePurchaseRepository.findById(transactionId)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found"));

        if(videoPackagePurchaseToRefund.getIsRefunded() != null && videoPackagePurchaseToRefund.getIsRefunded()) {
            throw new IllegalStateException("Can not refund this package purchase");
        }
        videoPackagePurchaseToRefund.setIsRefunded(true);
        videoPackagePurchaseRepository.save(videoPackagePurchaseToRefund);

        List<VideoPurchase> videoPurchasesToRefund = videoPurchaseRepository.findByPackagePurchaseId(videoPackagePurchaseToRefund.getId());
        BigDecimal treeToRefund = BigDecimal.ZERO;
        for (VideoPurchase videoPurchase : videoPurchasesToRefund) {
            videoPurchase.setIsRefunded(true);
            treeToRefund = treeToRefund.add(videoPurchase.getTreesConsumed());
            //TODO subtract DRM fee
        }
        videoPurchaseRepository.saveAll(videoPurchasesToRefund);

        String buyerId = videoPackagePurchaseToRefund.getUserId();
        String sellerId = videoPackagePurchaseToRefund.getSellerId();
        String packagePurchaseId = videoPackagePurchaseToRefund.getId();
        walletService.addToWallet(buyerId, treeToRefund, BigDecimal.ZERO, LocalDateTime.now());
        earningService.deductTreesFromEarning(sellerId, treeToRefund);
        ledgerService.saveToLedger(packagePurchaseId, treeToRefund, BigDecimal.ZERO, TransactionType.REFUND_PACKAGE_PURCHASE, buyerId);

        return ResponseEntity.ok(new GenericStringResponse(null, "Refund successfully"));
    }
}
