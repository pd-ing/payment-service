package com.pding.paymentservice.controllers;

import com.pding.paymentservice.service.PaymentStatisticService;
import com.pding.paymentservice.payload.request.CheckPurchaseVideoPackageRequest;
import com.pding.paymentservice.payload.request.VideoPurchaseStatusRequest;
import com.pding.paymentservice.service.VideoPackagePurchaseService;
import com.pding.paymentservice.service.VideoPurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalController {
    private final VideoPurchaseService videoPurchaseService;
    private final VideoPackagePurchaseService videoPackagePurchaseService;
    private final PaymentStatisticService paymentStatisticService;

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
}
