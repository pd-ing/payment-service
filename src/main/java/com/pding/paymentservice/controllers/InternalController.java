package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.Earning;
import com.pding.paymentservice.payload.request.PremiumEncodingFeeRequest;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.PremiumEncodingFeeResponse;
import com.pding.paymentservice.payload.response.UserEarningWalletResponse;
import com.pding.paymentservice.service.EarningService;
import com.pding.paymentservice.service.PaymentStatisticService;
import com.pding.paymentservice.payload.request.CheckPurchaseVideoPackageRequest;
import com.pding.paymentservice.payload.request.PackageSalesStatsRequest;
import com.pding.paymentservice.payload.request.VideoPurchaseStatusRequest;
import com.pding.paymentservice.service.PhotoPurchaseService;
import com.pding.paymentservice.service.PremiumEncodingTransactionService;
import com.pding.paymentservice.service.VideoPackagePurchaseService;
import com.pding.paymentservice.service.VideoPurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
@Slf4j
public class InternalController {
    private final VideoPurchaseService videoPurchaseService;
    private final VideoPackagePurchaseService videoPackagePurchaseService;
    private final PaymentStatisticService paymentStatisticService;
    private final PhotoPurchaseService photoPurchaseService;

    private final EarningService earningService;
    private final PdLogger pdLogger;
    private final PremiumEncodingTransactionService premiumEncodingTransactionService;

    @PostMapping("/checkIfVideoPurchasedExists")
    public ResponseEntity<?> checkIfVideoIsPurchased(@RequestParam(value = "videoId") String videoId) {
        return videoPurchaseService.isVideoPurchasedExists(videoId);
    }

    @PostMapping("/permanent-purchase-check")
    public ResponseEntity<Map<String, Boolean>> checkPermanentPurchase(
            @RequestBody VideoPurchaseStatusRequest request) {
        return videoPurchaseService.checkPermanentPurchase(request.getUserId(), request.getVideoIds());
    }

    @PostMapping("/monthly-revenue")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlyRevenue(
            @RequestParam String userId,
            @RequestParam(required = false, defaultValue = "12") Integer limit
    ) {
        try {
            Map<String, BigDecimal> result = paymentStatisticService.getMonthlyRevenue(userId, limit);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/package-purchase-check")
    public ResponseEntity<?> checkPurchasePackage(@RequestBody CheckPurchaseVideoPackageRequest request) {
        return videoPackagePurchaseService.checkPurchaseVideoPackage(request.getBuyerId(), request.getPackageIds());
    }

    /**
     * Get sales statistics for a list of package IDs
     *
     * @param request The request containing a list of package IDs
     * @return List of package sales statistics (package_id, quantity sold, total-tree-earned)
     */
    @PostMapping("/package-sales-stats")
    public ResponseEntity<?> getPackageSalesStats(@RequestBody PackageSalesStatsRequest request) {
        return videoPackagePurchaseService.getPackageSalesStats(request);
    }

    /**
     * Check if a list of photo posts have been purchased by a user
     *
     * @param userId  The ID of the user
     * @param postIds The list of post IDs to check
     * @return A map of post IDs to boolean values indicating if they have been purchased
     */
    @PostMapping("/check-photo-posts-purchased")
    public ResponseEntity<Map<String, Boolean>> checkPhotoPostsPurchased(
            @RequestParam(value = "userId") String userId,
            @RequestBody List<String> postIds) {
        Map<String, Boolean> result = photoPurchaseService.isPhotoPostPurchased(userId, postIds);
        return ResponseEntity.ok(result);
    }


    /**
     * Get a user's earning wallet balance
     *
     * @param userId The ID of the user
     * @return The user's earning wallet balance
     */
    @GetMapping("/user-earning-wallet")
    public ResponseEntity<UserEarningWalletResponse> getUserEarningWallet(@RequestParam(value = "userId") String userId) {
        log.info("Getting earning wallet balance for userId: {}", userId);

        try {
            Earning earning = earningService.findEarningByUserId(userId);

            return ResponseEntity.ok(new UserEarningWalletResponse(
                null,
                "Successfully retrieved earning wallet balance",
                userId,
                earning.getTreesEarned(),
                earning.getLeafsEarned()
            ));
        } catch (Exception e) {
            log.error("Error getting earning wallet balance for userId: {}", userId, e);
            pdLogger.logException(e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UserEarningWalletResponse(
                new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()),
                "Error getting earning wallet balance",
                userId,
                null,
                null
            ));
        }
    }

    @PostMapping("/deduct-premium-encoding-fee")
    public ResponseEntity<PremiumEncodingFeeResponse> deductPremiumEncodingFee(@RequestBody PremiumEncodingFeeRequest request) {
        log.info("Deducting premium encoding fee for userId: {}, videoId: {}, fee: {}",
            request.getUserId(), request.getVideoId(), request.getFee());

        try {
            Earning earning = earningService.findEarningByUserId(request.getUserId());
            BigDecimal currentBalance = earning.getTreesEarned();

            if (currentBalance.compareTo(request.getFee()) < 0) {
                log.error("Insufficient trees in earning wallet. Current balance: {}, Required: {}",
                    currentBalance, request.getFee());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new PremiumEncodingFeeResponse(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Insufficient trees in earning wallet"),
                    "Insufficient trees in earning wallet",
                    null,
                    currentBalance
                ));
            }

            earningService.deductTreesFromEarning(request.getUserId(), request.getFee());

            BigDecimal updatedBalance = earningService.findEarningByUserId(request.getUserId()).getTreesEarned();

            premiumEncodingTransactionService.createTransaction(
                request.getUserId(),
                request.getVideoId(),
                request.getFee(),
                "COMPLETED",
                "Premium encoding fee deducted from earning wallet"
            );

            log.info("Successfully deducted premium encoding fee for userId: {}, videoId: {}, fee: {}, remaining balance: {}",
                request.getUserId(), request.getVideoId(), request.getFee(), updatedBalance);

            return ResponseEntity.ok(new PremiumEncodingFeeResponse(
                null,
                "Premium encoding fee deducted successfully",
                request.getFee(),
                updatedBalance
            ));

        } catch (Exception e) {
            log.error("Error deducting premium encoding fee", e);
            pdLogger.logException(e);

            try {
                premiumEncodingTransactionService.createTransaction(
                    request.getUserId(),
                    request.getVideoId(),
                    request.getFee(),
                    "FAILED",
                    "Error: " + e.getMessage()
                );
            } catch (Exception ex) {
                log.error("Error saving failed transaction record", ex);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PremiumEncodingFeeResponse(
                new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()),
                "Error deducting premium encoding fee",
                null,
                null
            ));
        }
    }
}
