package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.request.CheckPurchaseVideoPackageRequest;
import com.pding.paymentservice.payload.request.PackageSalesStatsRequest;
import com.pding.paymentservice.payload.request.VideoPurchaseStatusRequest;
import com.pding.paymentservice.service.VideoPackagePurchaseService;
import com.pding.paymentservice.service.VideoPurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalController {
    private final VideoPurchaseService videoPurchaseService;
    private final VideoPackagePurchaseService videoPackagePurchaseService;

    @PostMapping("/permanent-purchase-check")
    public ResponseEntity<Map<String, Boolean>> checkPermanentPurchase(
            @RequestBody VideoPurchaseStatusRequest request) {
        return videoPurchaseService.checkPermanentPurchase(request.getUserId(), request.getVideoIds());
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
}
