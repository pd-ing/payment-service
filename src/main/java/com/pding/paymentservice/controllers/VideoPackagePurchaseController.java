package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.request.PurchaseVideoPackageRequest;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.VideoPackagePurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Controller for video package purchase operations
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment/packages")
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

    @GetMapping("/saleHistory")
    public ResponseEntity<?> packagePurchaseHistory(@RequestParam String packageId, Pageable pageable) {
        return videoPackagePurchaseService.getPackagePurchaseHistory(packageId, pageable);
    }

    @GetMapping("/dailySales")
    public ResponseEntity<?> dailySales(@RequestParam String packageId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return videoPackagePurchaseService.getDailySales(packageId, startDate, endDate);
    }
}
