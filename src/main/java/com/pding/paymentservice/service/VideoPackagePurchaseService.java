package com.pding.paymentservice.service;

import com.pding.paymentservice.listener.event.VideoPackagePurchaseUpdatedEvent;
import com.pding.paymentservice.models.VideoPackagePurchase;
import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.models.enums.PackageType;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.enums.VideoPurchaseDuration;
import com.pding.paymentservice.network.ContentNetworkService;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.dto.VideoPackagePurchaseDTO;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.net.VideoPackageDetailsResponseNet;
import com.pding.paymentservice.payload.net.VideoPackageItemDTONet;
import com.pding.paymentservice.payload.request.PackageSalesStatsRequest;
import com.pding.paymentservice.payload.request.PurchaseVideoPackageRequest;
import com.pding.paymentservice.payload.response.PackageSalesStatsResponse;
import com.pding.paymentservice.payload.response.PurchaseVideoPackageResponse;
import com.pding.paymentservice.payload.response.generic.GenericClassResponse;
import com.pding.paymentservice.payload.response.generic.GenericListDataResponse;
import com.pding.paymentservice.payload.response.generic.GenericPageResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import com.pding.paymentservice.repository.VideoPackagePurchaseRepository;
import com.pding.paymentservice.repository.VideoPurchaseRepository;
import com.pding.paymentservice.security.AuthHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
    private final UserServiceNetworkManager userServiceNetworkManager;
    private final LedgerService ledgerService;
    private final AuthHelper authHelper;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${video.purchase.drm.fee}")
    private BigDecimal drmFee;

    /**
     * Purchase a video package
     *
     * @param buyerId  The user ID
     * @param request The purchase request
     * @return Response with purchase details or error
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> purchaseVideoPackage(String buyerId, PurchaseVideoPackageRequest request) {
        String packageId = request.getPackageId();
        VideoPackageDetailsResponseNet packageDetail = contentNetworkService.getPackageDetails(packageId, request.getSelectedVideoIds())
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

        PurchaseVideoPackageResponse response;
        if (packageDetail.getPackageType() == PackageType.THEME_PACKAGE) {
            response = createThemePackageTransaction(buyerId, packageDetail, request);
        } else {
            response = createFreeChoicePackageTransaction(buyerId, packageDetail, request);
        }

        eventPublisher.publishEvent(new VideoPackagePurchaseUpdatedEvent(this, packageId));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private PurchaseVideoPackageResponse createFreeChoicePackageTransaction(String buyerId, VideoPackageDetailsResponseNet packageDetail, PurchaseVideoPackageRequest request) {

        if (request.getSelectedVideoIds() == null
            || request.getSelectedVideoIds().isEmpty()
            || request.getSelectedVideoIds().size() > packageDetail.getNumberOfVideos()) {
            throw new IllegalArgumentException("Invalid number of selected videos, must be between 1 and " + packageDetail.getNumberOfVideos() + " videos");
        }

        // Check if the selected videos belong to the package's seller
        if (request.getSelectedVideoIds().size() != packageDetail.getItems().size()) {
            throw new IllegalArgumentException("Invalid selected videos, video ids do not belong to this package's seller");
        }

        String packageId = packageDetail.getId();
        String sellerId = packageDetail.getSellerId();
        Set<String> alreadyPurchasedVideoIds = videoPurchaseRepository.findByUserIdAndPdId(buyerId, sellerId)
                .stream().map(VideoPurchase::getVideoId).collect(Collectors.toSet());

        // Check if any of the selected videos have already been purchased
        Set<String> intersection = new HashSet<>(alreadyPurchasedVideoIds);
        intersection.retainAll(request.getSelectedVideoIds());
        if (!intersection.isEmpty()) {
            throw new IllegalArgumentException("Some selected videos have already been purchased: " + intersection);
        }

        PurchaseVideoPackageResponse response = savePackagePurchaseTransaction(buyerId, packageDetail);
        return response;
    }
    private PurchaseVideoPackageResponse createThemePackageTransaction(String buyerId, VideoPackageDetailsResponseNet packageDetail, PurchaseVideoPackageRequest request) {
        String packageId = packageDetail.getId();
        List<VideoPackagePurchase> packagePurchased = videoPackagePurchaseRepository.findUnrefundedPackageByUserIdAndPackageIdForUpdate(buyerId, packageId);
        if (!packagePurchased.isEmpty()) {
            throw new IllegalStateException("Package already purchased");
        }

        PurchaseVideoPackageResponse response = savePackagePurchaseTransaction(buyerId, packageDetail);
        return response;
    }
    private PurchaseVideoPackageResponse savePackagePurchaseTransaction(String buyerId, VideoPackageDetailsResponseNet packageDetail) {
        String sellerId = packageDetail.getSellerId();
        String packageId = packageDetail.getId();
        List<VideoPackageItemDTONet> items = packageDetail.getItems();
        Map<String, BigDecimal> videoPrices = items.stream()
                .collect(Collectors.toMap(
                        VideoPackageItemDTONet::getVideoId,
                        VideoPackageItemDTONet::getPermanentPrice
                ));

        Map<String, Boolean> videoDrmEnabledMap = items.stream()
                .collect(Collectors.toMap(
                        VideoPackageItemDTONet::getVideoId,
                        VideoPackageItemDTONet::getDrmEnable
                ));

        Set<String> includedVideoIds = new HashSet<>();
        Set<String> ownedVideoIds = new HashSet<>();
        Integer discountPercentage = packageDetail.getDiscountPercentage();

        BigDecimal personalizedTotalPrice = calculatePersonalizedPrice(buyerId, items, videoPrices, discountPercentage, includedVideoIds, ownedVideoIds);
        walletService.deductTreesFromWallet(buyerId, personalizedTotalPrice);

        BigDecimal totalDrmFee = includedVideoIds.stream().filter(videoDrmEnabledMap::get)
                .map(s -> drmFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        earningService.addTreesToEarning(sellerId, personalizedTotalPrice.subtract(totalDrmFee));
        ledgerService.saveToLedger(packageId, personalizedTotalPrice, new BigDecimal(0), TransactionType.PACKAGE_PURCHASE, buyerId);

        VideoPackagePurchase packagePurchase = new VideoPackagePurchase(
                buyerId,
                packageId,
                sellerId,
                personalizedTotalPrice,
                includedVideoIds,
                ownedVideoIds,
                items.stream().map(VideoPackageItemDTONet::getPermanentPrice).reduce(BigDecimal.ZERO, BigDecimal::add),
                discountPercentage,
                totalDrmFee
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
                            buyerId,
                            videoId,
                            videoPrice.multiply(discountMultiplier).setScale(0, BigDecimal.ROUND_DOWN),
                            sellerId,
                            duration,
                            expiryDate,
                            discountPercentage,
                            packagePurchase.getId(),
                            videoDrmEnabledMap.containsKey(videoId) && videoDrmEnabledMap.get(videoId) ? drmFee : BigDecimal.ZERO
                    );
                }
        ).collect(Collectors.toList());
        videoPurchaseRepository.saveAll(videoPurchases);

        PurchaseVideoPackageResponse response = PurchaseVideoPackageResponse.builder()
                .packagePurchaseId(packagePurchase.getId())
                .packageId(packageId)
                .userId(buyerId)
                .sellerId(sellerId)
                .treesConsumed(personalizedTotalPrice)
                .purchaseDate(LocalDateTime.now())
                .includedVideoIds(includedVideoIds)
                .excludedVideoIds(ownedVideoIds)
                .build();
        return response;
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
        BigDecimal drmFee = BigDecimal.ZERO;
        for (VideoPurchase videoPurchase : videoPurchasesToRefund) {
            if(videoPurchase.getIsRefunded() != null && videoPurchase.getIsRefunded()) {
                continue;
            }
            drmFee = drmFee.add(videoPurchase.getDrmFee());
            videoPurchase.setIsRefunded(true);
            treeToRefund = treeToRefund.add(videoPurchase.getTreesConsumed());
        }
        videoPurchaseRepository.saveAll(videoPurchasesToRefund);

        String buyerId = videoPackagePurchaseToRefund.getUserId();
        String sellerId = videoPackagePurchaseToRefund.getSellerId();
        String packagePurchaseId = videoPackagePurchaseToRefund.getId();
        walletService.addToWallet(buyerId, treeToRefund, BigDecimal.ZERO, LocalDateTime.now());
        earningService.deductTreesFromEarning(sellerId, treeToRefund.subtract(drmFee));
        ledgerService.saveToLedger(packagePurchaseId, treeToRefund, BigDecimal.ZERO, TransactionType.REFUND_PACKAGE_PURCHASE, buyerId);

        eventPublisher.publishEvent(new VideoPackagePurchaseUpdatedEvent(this, videoPackagePurchaseToRefund.getPackageId()));

        return ResponseEntity.ok(new GenericStringResponse(null, "Refund successfully"));
    }

    public ResponseEntity<?> getPackagePurchaseHistory(String packageId, Pageable pageable) {
        String userId = authHelper.getUserId();

        VideoPackageDetailsResponseNet videoPackage = contentNetworkService.getPackageDetails(packageId, null).block();
        List<VideoPackageItemDTONet> items = videoPackage.getItems() != null? videoPackage.getItems() : List.of();
        Map<String, VideoPackageItemDTONet> videoIdToItemMap = items.stream()
                .collect(Collectors.toMap(VideoPackageItemDTONet::getVideoId, item -> item));

        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        Sort sort = Sort.by(Sort.Direction.DESC, "purchaseDate");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<VideoPackagePurchase> purchases = videoPackagePurchaseRepository.findByPackageIdAndSellerIdAndIsRefundedFalse(packageId, userId, pageRequest);

        Set<String> buyerIds = purchases.getContent().stream()
                .map(VideoPackagePurchase::getUserId)
                .collect(Collectors.toSet());

        List<PublicUserNet> buyers = userServiceNetworkManager.getUsersListFlux(buyerIds).blockLast();
        if(buyers == null) {buyers = List.of();}
        Map<String, PublicUserNet> buyerMap = buyers.stream()
                .collect(Collectors.toMap(PublicUserNet::getId, user -> user));

        Page<VideoPackagePurchaseDTO> result = purchases.map(
                videoPackagePurchase -> {
                    PublicUserNet buyer = buyerMap.get(videoPackagePurchase.getUserId());
                    List<VideoPackageItemDTONet> includedVideos = videoPackagePurchase.getIncludedVideoIdsList().stream()
                            .map(videoIdToItemMap::get)
                            .toList();

                    List<VideoPackageItemDTONet> excludedVideos = videoPackagePurchase.getExcludedVideoIdsList().stream()
                            .map(videoIdToItemMap::get)
                            .toList();

                    return VideoPackagePurchaseDTO.builder()
                            .id(videoPackagePurchase.getId())
                            .userId(videoPackagePurchase.getUserId())
                            .packageId(videoPackagePurchase.getPackageId())
                            .sellerId(videoPackagePurchase.getSellerId())
                            .treesConsumed(videoPackagePurchase.getTreesConsumed())
                            .purchaseDate(videoPackagePurchase.getPurchaseDate())
                            .includedVideoIds(videoPackagePurchase.getIncludedVideoIdsList())
                            .excludedVideoIds(videoPackagePurchase.getExcludedVideoIdsList())
                            .originalPrice(videoPackagePurchase.getOriginalPrice())
                            .discountPercentage(videoPackagePurchase.getDiscountPercentage())
                            .isRefunded(videoPackagePurchase.getIsRefunded())
                            .email(buyer != null ? buyer.getEmail() : null)
                            .includedVideos(includedVideos)
                            .excludedVideos(excludedVideos)
                            .build();
                }
        );
        return ResponseEntity.ok(new GenericPageResponse<>(null, result));
    }

    /**
     * Get sales statistics for a list of package IDs
     *
     * @param request The request containing a list of package IDs
     * @return List of package sales statistics (package_id, quantity sold, total-tree-earned)
     */
    public ResponseEntity<?> getPackageSalesStats(PackageSalesStatsRequest request) {
        List<String> packageIds = request.getPackageIds();
        if (packageIds == null || packageIds.isEmpty()) {
            return ResponseEntity.badRequest().body(new GenericStringResponse(null, "Package IDs list cannot be empty"));
        }

        // Get all non-refunded purchases for all package IDs in a single query
        List<VideoPackagePurchase> allPurchases = videoPackagePurchaseRepository.findByPackageIdInAndIsRefundedFalse(packageIds);

        // Group purchases by package ID
        Map<String, List<VideoPackagePurchase>> purchasesByPackageId = allPurchases.stream()
                .collect(Collectors.groupingBy(VideoPackagePurchase::getPackageId));

        List<PackageSalesStatsResponse> responseList = new ArrayList<>();

        // Process each package ID
        for (String packageId : packageIds) {
            List<VideoPackagePurchase> purchases = purchasesByPackageId.getOrDefault(packageId, List.of());

            // Calculate quantity sold
            int quantitySold = purchases.size();

            // Calculate total trees earned
            BigDecimal totalTreesEarned = purchases.stream()
                    .map(VideoPackagePurchase::getTreesConsumed)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalDrmFee = purchases.stream()
                    .map(VideoPackagePurchase::getDrmFee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Create response object
            PackageSalesStatsResponse stats = PackageSalesStatsResponse.builder()
                    .packageId(packageId)
                    .quantitySold(quantitySold)
                    .totalTreesEarned(totalTreesEarned.subtract(totalDrmFee))
                    .build();

            responseList.add(stats);
        }

        return ResponseEntity.ok(new GenericListDataResponse<>(null, responseList));
    }

    public ResponseEntity<?> getDailySales(String packageId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body(new GenericStringResponse(null, "Invalid date range"));
        }

        List<VideoPackagePurchase> purchases = videoPackagePurchaseRepository.findByPackageIdAndPurchaseDateBetweenAndIsRefundedFalse(
                packageId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

        Map<LocalDate, BigDecimal> dailySales = new HashMap<>();
        for (VideoPackagePurchase purchase : purchases) {
            LocalDate purchaseDate = purchase.getPurchaseDate().toLocalDate();
            dailySales.putIfAbsent(purchaseDate, BigDecimal.ZERO);
            dailySales.put(purchaseDate, dailySales.get(purchaseDate).add(purchase.getTreesConsumed()).subtract(purchase.getDrmFee()));
        }

        Map<LocalDate, Integer> dailySalesCount = new HashMap<>();
        for (VideoPackagePurchase purchase : purchases) {
            LocalDate purchaseDate = purchase.getPurchaseDate().toLocalDate();
            dailySalesCount.putIfAbsent(purchaseDate, 0);
            dailySalesCount.put(purchaseDate, dailySalesCount.get(purchaseDate) + 1);
        }

        Map<LocalDate, PackageSalesStatsResponse> responseMap = startDate.datesUntil(endDate.plusDays(1))
                .collect(Collectors.toMap(
                        date -> date,
                        date -> PackageSalesStatsResponse.builder()
                                .packageId(packageId)
                                .quantitySold(dailySalesCount.getOrDefault(date, 0))
                                .totalTreesEarned(dailySales.getOrDefault(date, BigDecimal.ZERO))
                                .build()
                ));

        return ResponseEntity.ok(new GenericClassResponse<>(null, responseMap));
    }
}


