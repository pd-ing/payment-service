package com.pding.paymentservice.controllers;

import com.pding.paymentservice.service.PaymentStatisticService;
import com.pding.paymentservice.service.VideoPurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/internal")
public class InternalController {
    @Autowired
    VideoPurchaseService videoPurchaseService;

    @Autowired
    PaymentStatisticService paymentStatisticService;

    @PostMapping("/checkIfVideoPurchasedExists")
    public ResponseEntity<?> checkIfVideoIsPurchased(@RequestParam(value = "videoId") String videoId) {
        return videoPurchaseService.isVideoPurchasedExists(videoId);
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
}
