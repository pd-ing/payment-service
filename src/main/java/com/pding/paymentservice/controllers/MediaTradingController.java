package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.request.AddMediaTrandingRequest;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.service.MediaTradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment/")
@RequiredArgsConstructor
public class MediaTradingController {
    private final MediaTradingService mediaTradingService;

    @PostMapping("/internal/mediaTrading")
    public ResponseEntity newMediaTrade(@RequestBody AddMediaTrandingRequest request) {
        return ResponseEntity.ok(mediaTradingService.saveMediaTrading(request));
    }

    @PostMapping(value = "/mediaTrading/cancel")
    public String cancelMediaTrade(@RequestBody AddMediaTrandingRequest request) {
        return "Cancel Media Trade";
    }

    @PostMapping(value = "/mediaTrading/buy")
    public ResponseEntity purchaseMediaTrade(@RequestParam String messageId) {
        try {
            mediaTradingService.buyMediaTrade(messageId);
            return ResponseEntity.ok(new ErrorResponse(HttpStatus.OK.value(), "Media purchased successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }
}
