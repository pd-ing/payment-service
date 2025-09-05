package com.pding.paymentservice.controllers;

import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.models.LiveStreamPurchase;
import com.pding.paymentservice.payload.request.BuyLiveStreamRequest;
import com.pding.paymentservice.payload.request.CheckLivestreamPurchaseRequest;
import com.pding.paymentservice.payload.response.BuyLiveStreamResponse;
import com.pding.paymentservice.payload.response.CheckLivestreamPurchaseResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.service.LiveStreamTradingService;
import com.pding.paymentservice.service.MissionTradingService;
import com.pding.paymentservice.payload.request.BuyMissionRequest;
import com.pding.paymentservice.payload.response.BuyMissionResponse;
import com.pding.paymentservice.models.MissionExecution;
import com.pding.paymentservice.models.enums.TransactionType;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment/live")
@RequiredArgsConstructor
@Slf4j
public class LiveStreamTradingController {

    private final LiveStreamTradingService livestreamTradingService;
    private final MissionTradingService missionTradingService;

    @PostMapping("/buy-access")
    public ResponseEntity<?> buyLivestreamAccess(@RequestBody BuyLiveStreamRequest request) {
        try {
            LiveStreamPurchase purchase = livestreamTradingService.processLivestreamPurchase(request);
            return ResponseEntity.ok(new BuyLiveStreamResponse(true, "Livestream access purchased successfully.", purchase.getId()));
        } catch (InsufficientTreesException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Error purchasing livestream access: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred."));
        }
    }

    @PostMapping("/check-purchase")
    public ResponseEntity<?> checkLivestreamPurchase(@RequestBody CheckLivestreamPurchaseRequest request) {
        try {
            boolean isPurchased = livestreamTradingService.isLivestreamPurchased(request.getLivestreamId());
            LiveStreamPurchase purchase = null;
            String message = isPurchased ? "Livestream access confirmed." : "Livestream not purchased.";

            if (isPurchased) {
                purchase = livestreamTradingService.getLivestreamPurchase(request.getLivestreamId());
            }

            return ResponseEntity.ok(new CheckLivestreamPurchaseResponse(
                isPurchased,
                message,
                purchase != null ? purchase.getId() : null
            ));
        } catch (Exception e) {
            log.error("Error checking livestream purchase status: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred."));
        }
    }

    @PostMapping("/mission-execution")
    public ResponseEntity<?> executeMission(@RequestBody BuyMissionRequest request) {
        try {
            MissionExecution execution = missionTradingService.executeMission(request);
            return ResponseEntity.ok(new BuyMissionResponse(true, "Mission executed successfully.", execution.getId()));
        } catch (InsufficientTreesException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Error executing mission: ", e); // Log the exception e
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred."));
        }
    }
}
