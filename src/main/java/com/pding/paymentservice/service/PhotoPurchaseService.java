package com.pding.paymentservice.service;

import com.pding.paymentservice.listener.event.PhotoPurchaseEvent;
import com.pding.paymentservice.models.PhotoPurchase;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.enums.VideoPurchaseDuration;
import com.pding.paymentservice.models.tables.inner.PhotoEarningsAndSales;
import com.pding.paymentservice.network.ContentNetworkService;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.net.PhotoPostResponseNet;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.net.VideoPurchaserInfo;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.PhotoEarningsAndSalesResponse;
import com.pding.paymentservice.payload.response.PhotoPurchaseTimeRemainingResponse;
import com.pding.paymentservice.payload.response.UserLite;
import com.pding.paymentservice.payload.response.custompagination.PaginationInfoWithGenericList;
import com.pding.paymentservice.payload.response.custompagination.PaginationResponse;
import com.pding.paymentservice.repository.PhotoPurchaseRepository;
import com.pding.paymentservice.util.LogSanitizer;
import com.pding.paymentservice.util.TokenSigner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for handling photo purchases for web
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PhotoPurchaseService {

    private final WalletService walletService;
    private final EarningService earningService;
    private final LedgerService ledgerService;
    private final PhotoPurchaseRepository photoPurchaseRepository;
    private final UserServiceNetworkManager userServiceNetworkManager;
    private final ContentNetworkService contentNetworkService;
    private final TokenSigner tokenSigner;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final XpAwardService xpAwardService;

    public ResponseEntity<?> loadPurchaseListOfSellerResponse(String photoId, int page, int size) {
        try {
            return ResponseEntity.ok(new PaginationResponse(null, loadPurchaseListOfSeller(photoId, page, size)));
        } catch (Exception ex) {
            log.error("Error loading purchase list of seller for photo: {}", photoId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
        }
    }

    private PaginationInfoWithGenericList<VideoPurchaserInfo> convertToResponse(Page<PhotoPurchase> dataList) throws Exception {
        List<PhotoPurchase> dataContent = dataList.getContent();
        Set<String> userIds = dataContent.stream().parallel().map(PhotoPurchase::getUserId).collect(Collectors.toSet());

        List<PublicUserNet> usersFlux = userServiceNetworkManager.getUsersListFlux(userIds).blockFirst();

        if (usersFlux == null) {
            return new PaginationInfoWithGenericList<>(
                dataList.getNumber(),
                dataList.getSize(),
                dataList.getTotalElements(),
                dataList.getTotalPages(),
                List.of()
            );
        }

        Map<String, PublicUserNet> userMap = usersFlux.stream().parallel().collect(Collectors.toMap(PublicUserNet::getId, user -> user));

        List<VideoPurchaserInfo> res = new ArrayList<>();

        dataContent.forEach((p) -> {
            PublicUserNet user = userMap.get(p.getUserId());
            if (user != null) {
                String date = p.getLastUpdateDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
                LocalDateTime expiryDateTime = p.getExpiryDate() != null ?
                    LocalDateTime.ofInstant(p.getExpiryDate(), ZoneId.systemDefault()) : null;
                res.add(new VideoPurchaserInfo(com.pding.paymentservice.util.StringUtil.maskEmail(user.getEmail()), p.getUserId(), null, date, p.getDuration(), expiryDateTime, p.getTreesConsumed(), user.getNickname()));
            }
        });

        return new PaginationInfoWithGenericList<>(
            dataList.getNumber(),
            dataList.getSize(),
            dataList.getTotalElements(),
            dataList.getTotalPages(),
            res
        );
    }

    private PaginationInfoWithGenericList<VideoPurchaserInfo> loadPurchaseListOfSeller(String photoId, int page, int size) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<PhotoPurchase> pageData = photoPurchaseRepository.findAllByPostIdOrderByLastUpdateDateDesc(photoId, pageRequest);

            return convertToResponse(pageData);
        } catch (Exception ex) {
            log.error("Error loading purchase list of seller for photo: {}", photoId, ex);
            return null;
        }
    }

    /**
     * Create a transaction for purchasing a photo post
     *
     * @param userId          The ID of the user making the purchase
     * @param postId          The ID of the post being purchased
     * @param treesConsumed   The amount of trees to be paid
     * @param postOwnerUserId The ID of the post owner
     * @return The created transaction
     */
    public PhotoPurchase createPhotoPostTransaction(String userId, String postId, BigDecimal treesConsumed, String postOwnerUserId) {
        log.info("Buy photo request made with following details UserId: {}, postId: {}, treesConsumed: {}, postOwnerUserId: {}",
                LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(postId),
                LogSanitizer.sanitizeForLog(treesConsumed), LogSanitizer.sanitizeForLog(postOwnerUserId));

        walletService.deductTreesFromWallet(userId, treesConsumed);

        PhotoPurchase transaction = new PhotoPurchase(userId, postId, treesConsumed, postOwnerUserId);
        PhotoPurchase savedTransaction = photoPurchaseRepository.save(transaction);

        log.info("Photo purchase record created with details UserId: {}, postId: {}, treesConsumed: {}, postOwnerUserId: {}",
                LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(postId),
                LogSanitizer.sanitizeForLog(treesConsumed), LogSanitizer.sanitizeForLog(postOwnerUserId));

        earningService.addTreesToEarning(postOwnerUserId, treesConsumed);
        ledgerService.saveToLedger(savedTransaction.getId(), treesConsumed, new BigDecimal(0), TransactionType.IMAGE_PURCHASE, userId);

        log.info("Buy photo request transaction completed with details UserId: {}, postId: {}, treesConsumed: {}, postOwnerUserId: {}",
                LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(postId),
                LogSanitizer.sanitizeForLog(treesConsumed), LogSanitizer.sanitizeForLog(postOwnerUserId));

        return savedTransaction;
    }

    /**
     * Create a transaction for purchasing a photo post with a specific duration
     *
     * @param userId          The ID of the user making the purchase
     * @param postId          The ID of the post being purchased
     * @param treesConsumed   The amount of trees to be paid
     * @param postOwnerUserId The ID of the post owner
     * @param duration        The duration of the purchase
     * @param expiryDate      The expiry date of the purchase
     * @return The created transaction
     */
    public PhotoPurchase createPhotoPostTransaction(String userId, String postId, BigDecimal treesConsumed, String postOwnerUserId, String duration, Instant expiryDate) {
        log.info("Buy photo request made with following details UserId: {}, postId: {}, treesConsumed: {}, postOwnerUserId: {}, duration: {}",
                LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(postId),
                LogSanitizer.sanitizeForLog(treesConsumed), LogSanitizer.sanitizeForLog(postOwnerUserId),
                LogSanitizer.sanitizeForLog(duration));

        walletService.deductTreesFromWallet(userId, treesConsumed);

        PhotoPurchase transaction = new PhotoPurchase(userId, postId, treesConsumed, postOwnerUserId, duration, expiryDate);
        PhotoPurchase savedTransaction = photoPurchaseRepository.save(transaction);

        log.info("Photo purchase record created with details UserId: {}, postId: {}, treesConsumed: {}, postOwnerUserId: {}, duration: {}",
                LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(postId),
                LogSanitizer.sanitizeForLog(treesConsumed), LogSanitizer.sanitizeForLog(postOwnerUserId),
                LogSanitizer.sanitizeForLog(duration));

        earningService.addTreesToEarning(postOwnerUserId, treesConsumed);
        ledgerService.saveToLedger(savedTransaction.getId(), treesConsumed, new BigDecimal(0), TransactionType.WEB_PHOTO_PURCHASE, userId);

        log.info("Buy photo request transaction completed with details UserId: {}, postId: {}, treesConsumed: {}, postOwnerUserId: {}, duration: {}",
                LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(postId),
                LogSanitizer.sanitizeForLog(treesConsumed), LogSanitizer.sanitizeForLog(postOwnerUserId),
                LogSanitizer.sanitizeForLog(duration));

        return savedTransaction;
    }

    /**
     * Buy a photo post
     *
     * @param userId The ID of the user making the purchase
     * @param postId The ID of the post being purchased
     * @return The created transaction
     */
    @Transactional
    public PhotoPurchase buyPhotoPost(String userId, String postId) {
        validate(userId, postId);

        // Get photo post details from content service
        PhotoPostResponseNet photoPost = contentNetworkService.getPhotoPostDetails(postId)
            .blockOptional()
            .orElseThrow(() -> new RuntimeException("Cannot find photo post with ID: " + postId));

        String postOwnerUserId = photoPost.getUserId();
        BigDecimal treesConsumed = photoPost.getTrees();

        if (treesConsumed == null || postOwnerUserId == null) {
            throw new RuntimeException("Cannot find post owner or trees amount");
        }

        return createPhotoPostTransaction(userId, postId, treesConsumed, postOwnerUserId);
    }

    /**
     * Buy a photo post with a specific duration
     *
     * @param userId   The ID of the user making the purchase
     * @param postId   The ID of the post being purchased
     * @param duration The duration of the purchase
     * @return The created transaction
     */
    @Transactional(rollbackFor = Exception.class)
    public PhotoPurchase buyPhotoPost(String userId, String postId, String duration) {
        validate(userId, postId, duration);

        PhotoPostResponseNet photoPost = contentNetworkService.getPhotoPostDetails(postId)
            .blockOptional()
            .orElseThrow(() -> new RuntimeException("Cannot find photo post with ID: " + postId));

        String postOwnerUserId = photoPost.getUserId();

        PhotoPostResponseNet.PhotoDurationPriceDTO price = photoPost.getPrices().stream()
            .filter(p -> p.getDuration().equals(duration) && p.getEnabled())
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Cannot find price for duration: " + duration + " or the duration is not enabled"));

        BigDecimal treesConsumed = price.getTrees();

        if (treesConsumed == null || postOwnerUserId == null) {
            throw new RuntimeException("Cannot find post owner or trees amount");
        }

        Instant expiryDate = VideoPurchaseDuration.valueOf(duration).getExpiryDateInstant();

        PhotoPurchase photoPurchase = createPhotoPostTransaction(userId, postId, treesConsumed, postOwnerUserId, duration, expiryDate);
        applicationEventPublisher.publishEvent(new PhotoPurchaseEvent(this, photoPurchase, photoPost));
        
        // Award XP for tree usage
        xpAwardService.awardXpForTreeUsage(userId, treesConsumed, postId, "photo");
        
        return photoPurchase;
    }

    /**
     * Validate the purchase request
     *
     * @param userId The ID of the user making the purchase
     * @param postId The ID of the post being purchased
     */
    private void validate(String userId, String postId) {
        if (userId == null || postId == null) {
            throw new IllegalArgumentException("Invalid request parameters");
        }

        if (photoPurchaseRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new IllegalArgumentException("User already purchased this post");
        }
    }

    /**
     * Validate the purchase request with duration
     *
     * @param userId   The ID of the user making the purchase
     * @param postId   The ID of the post being purchased
     * @param duration The duration of the purchase
     */
    private void validate(String userId, String postId, String duration) {
        if (userId == null || postId == null || duration == null) {
            throw new IllegalArgumentException("Invalid request parameters");
        }

        // Check if the user has already purchased this post with a duration that hasn't expired
        List<PhotoPurchase> purchases = photoPurchaseRepository.findByUserIdAndPostId(userId, postId);

        if (purchases != null && !purchases.isEmpty()) {
            // Check if any purchase has not expired
            boolean hasNonExpiredPurchase = purchases.stream()
                .anyMatch(purchase -> purchase.getExpiryDate() == null || purchase.getExpiryDate().isAfter(Instant.now()));

            if (hasNonExpiredPurchase) {
                throw new IllegalArgumentException("User already has an active purchase for this post");
            }
        }
    }

    /**
     * Check if a list of posts have been purchased by a user
     *
     * @param userId  The ID of the user
     * @param postIds The list of post IDs to check
     * @return A map of post IDs to PhotoPurchaseTimeRemainingResponse objects containing purchase information
     */
    public Map<String, PhotoPurchaseTimeRemainingResponse> isPhotoPostPurchased(String userId, List<String> postIds) {
        List<PhotoPurchase> photoPurchases = photoPurchaseRepository.findByUserIdAndPostIdIn(userId, postIds);

        // Group purchases by post ID
        Map<String, List<PhotoPurchase>> purchasesByPostId = photoPurchases.stream()
            .collect(Collectors.groupingBy(PhotoPurchase::getPostId));

        // Create a map of post IDs to purchase information
        Map<String, PhotoPurchaseTimeRemainingResponse> result = new HashMap<>();

        for (String postId : postIds) {
            if (postId == null) continue;

            List<PhotoPurchase> purchases = purchasesByPostId.get(postId);
            if (purchases == null || purchases.isEmpty()) {
                // No purchases for this post
                continue;
            }

            // Find the best purchase (permanent or with the latest expiry date)
            PhotoPurchase bestPurchase = purchases.stream()
                .filter(purchase -> purchase.getExpiryDate() == null || purchase.getExpiryDate().isAfter(Instant.now()))
                .findFirst()
                .orElse(null);

            if (bestPurchase == null) {
                // No valid purchases for this post
                continue;
            }

            // Create response object
            PhotoPurchaseTimeRemainingResponse response = new PhotoPurchaseTimeRemainingResponse();
            response.setPhotoPostId(postId);
            response.setExpiryDate(bestPurchase.getExpiryDate());
            response.setIsPermanent(bestPurchase.getExpiryDate() == null || bestPurchase.getDuration() == null || bestPurchase.getDuration().equals(VideoPurchaseDuration.PERMANENT.name()));
            response.setIsExpirated(bestPurchase.getExpiryDate() != null && bestPurchase.getExpiryDate().isBefore(Instant.now()));

            // Calculate days remaining if not permanent
            if (bestPurchase.getExpiryDate() != null) {
                long daysRemaining = ChronoUnit.DAYS.between(Instant.now(), bestPurchase.getExpiryDate());
                response.setNumberOfDaysRemaining(daysRemaining);
            }

            result.put(postId, response);
        }

        return result;
    }

    /**
     * Get all photo posts purchased by a user from a specific creator
     *
     * @param userId The ID of the user
     * @param pdId   The ID of the creator
     * @param page   The page number
     * @param size   The page size
     * @return A slice of photo purchases
     */
    public Slice<PhotoPurchase> getPurchasedPhotoPosts(String userId, String pdId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("lastUpdateDate").descending());
        return photoPurchaseRepository.findByUserIdAndPostOwnerUserId(userId, pdId, pageable);
    }

    /**
     * Get all creators whose photo posts have been purchased by a user
     *
     * @param userId The ID of the user
     * @param size   The page size
     * @param page   The page number
     * @return A page of user lite objects
     * @throws Exception If there is an error getting the user information
     */
    public Page<UserLite> getAllPdUserIdWhosePhotosArePurchasedByUser(String userId, int size, int page) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        Page<String> userIdsPage = photoPurchaseRepository.getAllPdUserIdWhosePostsArePurchasedByUser(userId, pageable);

        if (userIdsPage.isEmpty()) {
            return Page.empty();
        }

        List<PublicUserNet> usersFlux = userServiceNetworkManager.getUsersListFlux(userIdsPage.toSet()).blockFirst();

        if (usersFlux == null) {
            return Page.empty();
        }

        List<UserLite> users = usersFlux.stream().map(userObj -> UserLite.fromPublicUserNet(userObj, tokenSigner)).toList();
        return new PageImpl<>(users, pageable, userIdsPage.getTotalElements());
    }

    /**
     * Get earnings and sales data for photo posts
     *
     * @param photoPostIds Comma-separated list of photo post IDs
     * @return Response with earnings and sales data for each photo post
     */
    public ResponseEntity<?> photoEarningAndSales(String photoPostIds) {
        if (photoPostIds == null || photoPostIds.isEmpty()) {
            return ResponseEntity.badRequest().body(new PhotoEarningsAndSalesResponse(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "photoPostIds parameter is required."), null));
        }
        try {
            List<String> photoPostIdsList = Arrays.stream(photoPostIds.split(","))
                .toList();
            Map<String, PhotoEarningsAndSales> photoStats = photoPurchaseRepository.getTotalTreesEarnedAndSalesCountMapForPostIds(photoPostIdsList);
            return ResponseEntity.ok().body(new PhotoEarningsAndSalesResponse(null, photoStats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PhotoEarningsAndSalesResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }
}
