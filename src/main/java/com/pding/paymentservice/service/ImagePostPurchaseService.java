package com.pding.paymentservice.service;

import com.pding.paymentservice.models.ImagePurchase;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.network.ContentNetworkService;
import com.pding.paymentservice.network.UserServiceNetworkManager;
import com.pding.paymentservice.payload.net.PhotoPostResponseNet;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.response.UserLite;
import com.pding.paymentservice.repository.ImagePurchaseRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImagePostPurchaseService {

    private final WalletService walletService;
    private final EarningService earningService;
    private final LedgerService ledgerService;
    private final ImagePurchaseRepository imagePurchaseRepository;
    private final UserServiceNetworkManager userServiceNetworkManager;
    private final TokenSigner tokenSigner;

    @Transactional
    public ImagePurchase createImagePostTransaction(String userId, String postId, BigDecimal leafAmount, String postOwnerUserId) {
        log.info("Buy image request made with following details UserId : {} ,postId : {}, leafAmount : {}, postOwnerUserId : {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(postId), LogSanitizer.sanitizeForLog(leafAmount), LogSanitizer.sanitizeForLog(postOwnerUserId));
        walletService.deductLeafsFromWallet(userId, leafAmount);

        ImagePurchase transaction = new ImagePurchase(userId, postId, leafAmount, postOwnerUserId);
        ImagePurchase savedTransaction = imagePurchaseRepository.save(transaction);
        log.info("Image purchase record created with details UserId : {} ,postId : {}, leafAmount : {}, postOwnerUserId : {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(postId), LogSanitizer.sanitizeForLog(leafAmount), LogSanitizer.sanitizeForLog(postOwnerUserId));

        earningService.addLeafsToEarning(postOwnerUserId, leafAmount);
        ledgerService.saveToLedger(savedTransaction.getId(), new BigDecimal(0), savedTransaction.getLeafAmount(), TransactionType.IMAGE_PURCHASE, userId);
        log.info("Buy video request transaction completed with details UserId : {} ,postId : {}, leafAmount : {}, postOwnerUserId : {}", LogSanitizer.sanitizeForLog(userId), LogSanitizer.sanitizeForLog(postId), LogSanitizer.sanitizeForLog(leafAmount), LogSanitizer.sanitizeForLog(postOwnerUserId));
        return savedTransaction;
    }

    @Transactional
    public ImagePurchase buyImagePost(String userId, String postId) {
        validate(userId, postId);
        String postOwnerUserId = null;
        BigDecimal leafAmount = null;
        List<Object[]> ownerAndPriceByPostId = imagePurchaseRepository.fetchOwnerAndPriceByPostId(postId);
        for (Object[] row : ownerAndPriceByPostId) {
            postOwnerUserId = (String) row[0];
            leafAmount = (BigDecimal) row[1];
        }
        if (leafAmount == null || postOwnerUserId == null) {
            throw new RuntimeException("cannot find post owner or leaf amount");
        }

        return createImagePostTransaction(userId, postId, leafAmount, postOwnerUserId);
    }

    private void validate(String userId, String postId) {

        if (userId == null || postId == null) {
            throw new IllegalArgumentException("Invalid request parameters");
        }

        if (imagePurchaseRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new IllegalArgumentException("User already purchased this post");
        }
    }

    public Map<String, Boolean> isImagePostPurchased(String userId, List<String> postIds) {
        List<ImagePurchase> imagePurchases = imagePurchaseRepository.findByUserIdAndPostIdIn(userId, postIds);
        List<String> purchasedPostIds = imagePurchases.stream().map(ImagePurchase::getPostId).collect(Collectors.toList());
        return postIds.stream().filter(Objects::nonNull).collect(Collectors.toMap(postId -> postId, postId -> purchasedPostIds.contains(postId)));
    }

    public Slice<ImagePurchase> getPurchasedImagePosts(String userId, String pdId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("lastUpdateDate").descending());
        return imagePurchaseRepository.findByUserIdAndPostOwnerUserId(userId, pdId, pageable);
    }

    public Page<UserLite> getAllPdUserIdWhoseVideosArePurchasedByUser(String userId, int size, int page) throws Exception {

        Pageable pageable = PageRequest.of(page, size);
        Page<String> userIdsPage = imagePurchaseRepository.getAllPdUserIdWhosePostsArePurchasedByUser(userId, pageable);

        if (userIdsPage.isEmpty()) {
            return Page.empty();
        }

        List<PublicUserNet> usersFlux = userServiceNetworkManager.getUsersListFlux(userIdsPage.toSet()).blockFirst();

        if (usersFlux == null) {
            return Page.empty();
        }

        List<UserLite> users = usersFlux.stream().map(userObj -> UserLite.fromPublicUserNet(userObj, tokenSigner)).toList();
        Page<UserLite> resData = new PageImpl<>(users, pageable, userIdsPage.getTotalElements());

        return resData;

    }
}
