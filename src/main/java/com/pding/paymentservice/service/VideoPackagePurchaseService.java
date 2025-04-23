package com.pding.paymentservice.service;

import com.pding.paymentservice.models.VideoPackagePurchase;
import com.pding.paymentservice.models.VideoPurchase;
import com.pding.paymentservice.models.Wallet;
import com.pding.paymentservice.payload.request.PurchaseVideoPackageRequest;
import com.pding.paymentservice.payload.response.PurchaseVideoPackageResponse;
import com.pding.paymentservice.repository.VideoPackagePurchaseRepository;
import com.pding.paymentservice.repository.VideoPurchaseRepository;
import com.pding.paymentservice.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling video package purchases
 */
@Service
public class VideoPackagePurchaseService {

    @Autowired
    private VideoPackagePurchaseRepository videoPackagePurchaseRepository;

    @Autowired
    private VideoPurchaseRepository videoPurchaseRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Purchase a video package
     * @param userId The user ID
     * @param request The purchase request
     * @return Response with purchase details or error
     */
    @Transactional
    public ResponseEntity<?> purchaseVideoPackage(String userId, PurchaseVideoPackageRequest request) {
        // Check if user already purchased this package
        if (videoPackagePurchaseRepository.existsByUserIdAndPackageId(userId, request.getPackageId())) {
            return ResponseEntity.badRequest().body("You have already purchased this package");
        }

        // Get package details from content-service
        // In a real implementation, this would call the content-service API
        // For now, we'll use the data from the request
        String packageId = request.getPackageId();
        String sellerId = request.getSellerId();
        BigDecimal personalizedPrice = request.getPersonalizedPrice();
        BigDecimal originalPrice = request.getOriginalPrice();
        Integer discountPercentage = request.getDiscountPercentage();
        List<String> videoIds = request.getVideoIds();
        List<String> ownedVideoIds = request.getOwnedVideoIds();

        // Check if user has enough trees
        Optional<Wallet> walletOpt = walletRepository.findWalletByUserId(userId);
        if (walletOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Wallet not found");
        }

        Wallet wallet = walletOpt.get();
        if (wallet.getTrees().compareTo(personalizedPrice) < 0) {
            return ResponseEntity.badRequest().body("Insufficient funds");
        }

        // Deduct trees from wallet
        wallet.setTrees(wallet.getTrees().subtract(personalizedPrice));
        walletRepository.save(wallet);

        // Create package purchase record
        List<String> includedVideoIds = new ArrayList<>(videoIds);
        includedVideoIds.removeAll(ownedVideoIds);

        VideoPackagePurchase packagePurchase = new VideoPackagePurchase(
                userId,
                packageId,
                sellerId,
                personalizedPrice,
                includedVideoIds,
                ownedVideoIds,
                originalPrice,
                discountPercentage
        );
        videoPackagePurchaseRepository.save(packagePurchase);

        // Create individual video purchases for each included video
        List<VideoPurchase> videoPurchases = new ArrayList<>();
        for (String videoId : includedVideoIds) {
            // In a real implementation, we would calculate the individual video price
            // For now, we'll use a simplified approach
            BigDecimal videoPrice = personalizedPrice.divide(new BigDecimal(includedVideoIds.size()));

            VideoPurchase videoPurchase = new VideoPurchase(
                    userId,
                    videoId,
                    videoPrice,
                    sellerId,
                    "PERMANENT",
                    null,
                    BigDecimal.ZERO // No DRM fee for package purchases
            );
            videoPurchaseRepository.save(videoPurchase);
            videoPurchases.add(videoPurchase);
        }

        // Create response
        PurchaseVideoPackageResponse response = PurchaseVideoPackageResponse.builder()
                .packagePurchaseId(packagePurchase.getId())
                .packageId(packageId)
                .userId(userId)
                .sellerId(sellerId)
                .treesConsumed(personalizedPrice)
                .purchaseDate(LocalDateTime.now())
                .includedVideoIds(includedVideoIds)
                .excludedVideoIds(ownedVideoIds)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all package purchases by user ID
     * @param userId The user ID
     * @return List of package purchases
     */
    public ResponseEntity<?> getPackagePurchasesByUserId(String userId) {
        List<VideoPackagePurchase> purchases = videoPackagePurchaseRepository.findByUserId(userId);
        return ResponseEntity.ok(purchases);
    }

    /**
     * Get all package purchases by seller ID
     * @param sellerId The seller ID
     * @return List of package purchases
     */
    public ResponseEntity<?> getPackagePurchasesBySellerId(String sellerId) {
        List<VideoPackagePurchase> purchases = videoPackagePurchaseRepository.findBySellerId(sellerId);
        return ResponseEntity.ok(purchases);
    }

    /**
     * Check if a user has purchased a specific package
     * @param userId The user ID
     * @param packageId The package ID
     * @return True if the user has purchased the package
     */
    public boolean hasUserPurchasedPackage(String userId, String packageId) {
        return videoPackagePurchaseRepository.existsByUserIdAndPackageId(userId, packageId);
    }
}
