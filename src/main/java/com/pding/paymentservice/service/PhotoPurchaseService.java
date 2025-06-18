package com.pding.paymentservice.service;

import com.pding.paymentservice.models.PhotoPurchase;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.models.enums.VideoPurchaseDuration;
import com.pding.paymentservice.network.ContentNetworkService;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.net.PhotoPostResponseNet;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.response.UserLite;
import com.pding.paymentservice.repository.PhotoPurchaseRepository;
import com.pding.paymentservice.util.LogSanitizer;
import com.pding.paymentservice.util.TokenSigner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        return createPhotoPostTransaction(userId, postId, treesConsumed, postOwnerUserId, duration, expiryDate);
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
     * @return A map of post IDs to boolean values indicating if they have been purchased
     */
    public Map<String, Boolean> isPhotoPostPurchased(String userId, List<String> postIds) {
        List<PhotoPurchase> photoPurchases = photoPurchaseRepository.findByUserIdAndPostIdIn(userId, postIds);

        // Filter purchases to only include those that haven't expired (expiry_date > now())
        List<String> purchasedPostIds = photoPurchases.stream()
            .filter(purchase -> purchase.getExpiryDate() == null || purchase.getExpiryDate().isAfter(Instant.now()))
            .map(PhotoPurchase::getPostId)
            .collect(Collectors.toList());

        return postIds.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(postId -> postId, postId -> purchasedPostIds.contains(postId)));
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
}
