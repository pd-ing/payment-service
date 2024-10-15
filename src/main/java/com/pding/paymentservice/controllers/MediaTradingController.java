package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.request.AddMediaTrandingRequest;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.MediaTradingResponse;
import com.pding.paymentservice.payload.response.generic.GenericSliceResponse;
import com.pding.paymentservice.service.MediaTradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity cancelMediaTrade(@RequestParam String messageId) {
        try {
            mediaTradingService.cancelMediaTrade(messageId);
            return ResponseEntity.ok(new ErrorResponse(HttpStatus.OK.value(), "Media canceled successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
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

    @GetMapping(value = "/mediaTrading")
    public ResponseEntity getMediaTrade(@RequestParam("userId") String userId, @RequestParam("pdId") String pdId, Pageable pageable) {
        Slice<MediaTradingResponse> mediaTradeSlice = mediaTradingService.getMediaTrade(userId, pdId, pageable);

        return ResponseEntity.ok(new GenericSliceResponse<>(null, mediaTradeSlice.getContent(), mediaTradeSlice.hasNext()));
    }
}
