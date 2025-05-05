package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.request.PurchaseVideoPackageRequest;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.VideoPackagePurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for video package purchase operations
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payments/packages")
public class VideoPackagePurchaseController {

    @Autowired
    private VideoPackagePurchaseService videoPackagePurchaseService;

    @Autowired
    private AuthHelper authHelper;

    /**
     * Purchase a video package
     * @param request The purchase request
     * @return The purchase details or error
     */
    @PostMapping("/purchase")
    public ResponseEntity<?> purchasePackage(@RequestBody PurchaseVideoPackageRequest request) {
            String userId = authHelper.getUserId();
            return videoPackagePurchaseService.purchaseVideoPackage(userId, request);
    }

    @PostMapping("/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> refundPackagePurchase(@RequestParam String transactionId) {
            return videoPackagePurchaseService.refundPackagePurchase(transactionId);
    }

    @PostMapping("/saleHistory")
    public ResponseEntity<?> packagePurchaseHistory(@RequestParam String packageId, Pageable pageable) {
        return videoPackagePurchaseService.getPackagePurchaseHistory(packageId, pageable);
    }

    /**
     * Get all package purchases by the current user
     * @return List of package purchases
     */
//    @GetMapping("/user")
//    public ResponseEntity<?> getUserPackagePurchases() {
//        try {
//            String userId = authHelper.getUserId();
//            return videoPackagePurchaseService.getPackagePurchasesByUserId(userId);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Authentication error: " + e.getMessage());
//        }
//    }

    /**
     * Get all package purchases by the current seller
     * @return List of package purchases
     */
//    @GetMapping("/seller")
//    public ResponseEntity<?> getSellerPackagePurchases() {
//        try {
//            String sellerId = authHelper.getUserId();
//            return videoPackagePurchaseService.getPackagePurchasesBySellerId(sellerId);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Authentication error: " + e.getMessage());
//        }
//    }

    /**
     * Check if the current user has purchased a specific package
     * @param packageId The package ID
     * @return True if the user has purchased the package
     */
//    @GetMapping("/check/{packageId}")
//    public ResponseEntity<?> checkPackagePurchase(@PathVariable String packageId) {
//        try {
//            String userId = authHelper.getUserId();
//            boolean hasPurchased = videoPackagePurchaseService.hasUserPurchasedPackage(userId, packageId);
//            return ResponseEntity.ok(hasPurchased);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Authentication error: " + e.getMessage());
//        }
//    }
}
