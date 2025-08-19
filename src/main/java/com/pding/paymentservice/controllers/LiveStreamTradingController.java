package com.pding.paymentservice.controllers;

import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.models.LivestreamPurchase;
import com.pding.paymentservice.payload.request.BuyLivestreamRequest;
import com.pding.paymentservice.payload.response.BuyLivestreamResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.service.LivestreamTradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment/livestream")
@RequiredArgsConstructor
public class LivestreamTradingController {

    private final LivestreamTradingService livestreamTradingService;

    @PostMapping("/buy-access")
    public ResponseEntity<?> buyLivestreamAccess(@RequestBody BuyLivestreamRequest request) {
        try {
            LivestreamPurchase purchase = livestreamTradingService.processLivestreamPurchase(request);
            return ResponseEntity.ok(new BuyLivestreamResponse(true, "Livestream access purchased successfully.", purchase.getId()));
        } catch (InsufficientTreesException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("INSUFFICIENT_FUNDS", "You do not have enough trees to make this purchase."));
        } catch (Exception e) {
            // Log the exception e
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("PURCHASE_FAILED", "An unexpected error occurred. Please try again."));
        }
    }
}