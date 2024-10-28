package com.pding.paymentservice.service;

import com.pding.paymentservice.models.ImagePurchase;
import com.pding.paymentservice.models.enums.TransactionType;
import com.pding.paymentservice.repository.ImagePurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public ImagePurchase createImagePostTransaction(String userId, String postId, BigDecimal leafAmount, String postOwnerUserId) {
        log.info("Buy image request made with following details UserId : {} ,postId : {}, leafAmount : {}, postOwnerUserId : {}", userId, postId, leafAmount, postOwnerUserId);
        walletService.deductLeafsFromWallet(userId, leafAmount);

        ImagePurchase transaction = new ImagePurchase(userId, postId, leafAmount, postOwnerUserId);
        ImagePurchase savedTransaction = imagePurchaseRepository.save(transaction);
        log.info("Image purchase record created with details UserId : {} ,postId : {}, leafAmount : {}, postOwnerUserId : {}", userId, postId, leafAmount, postOwnerUserId);

        earningService.addLeafsToEarning(postOwnerUserId, leafAmount);
        ledgerService.saveToLedger(savedTransaction.getId(), new BigDecimal(0), savedTransaction.getLeafAmount(), TransactionType.IMAGE_PURCHASE, userId);
        log.info("Buy video request transaction completed with details UserId : {} ,postId : {}, leafAmount : {}, postOwnerUserId : {}", userId, postId, leafAmount, postOwnerUserId);
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
        if(leafAmount == null || postOwnerUserId == null) {
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
}
