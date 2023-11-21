package com.pding.paymentservice.controllers;

import com.pding.paymentservice.exception.InsufficientTreesException;
import com.pding.paymentservice.exception.InvalidAmountException;
import com.pding.paymentservice.exception.WalletNotFoundException;
import com.pding.paymentservice.models.VideoTransactions;
import com.pding.paymentservice.payload.response.BuyVideoResponse;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.GetVideoTransactionsResponse;
import com.pding.paymentservice.payload.response.IsVideoPurchasedByUserResponse;
import com.pding.paymentservice.payload.response.TotalTreesEarnedResponse;
import com.pding.paymentservice.service.VideoTransactionsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class VideoPurchaseServiceController {

    @Autowired
    VideoTransactionsService videoTransactionsService;

    @PostMapping(value = "/buyVideo")
    public ResponseEntity<?> buyVideo(@RequestParam(value = "userId") String userId, @RequestParam(value = "videoId") String videoId, @RequestParam(value = "trees") BigDecimal trees, @RequestParam(value = "videoOwnerUserId") String videoOwnerUserId, HttpServletRequest request) {
        return videoTransactionsService.buyVideo(userId, videoId, trees, videoOwnerUserId);
    }

    @GetMapping(value = "/videoTransactions")
    public ResponseEntity<?> getVideotransactions(@RequestParam(value = "userId") String userId, HttpServletRequest request) {
        return videoTransactionsService.getVideoTransactions(userId);
    }

    @GetMapping(value = "/treesEarned")
    public ResponseEntity<?> getTotalTreesEarnedByVideoOwner(@RequestParam(value = "videoOwnerUserId") String videoOwnerUserId) {
        return videoTransactionsService.getTreesEarned(videoOwnerUserId);
    }

    @GetMapping(value = "/isVideoPurchased")
    public ResponseEntity<?> isVideoPurchasedByUser(@RequestParam(value = "userId") String userId, @RequestParam(value = "videoId") String videoId) {
        return videoTransactionsService.isVideoPurchased(userId, videoId);
    }


    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }

}
