package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.VideoPurchaseService;
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

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class VideoPurchaseServiceController {

    @Autowired
    VideoPurchaseService videoPurchaseService;

    @PostMapping(value = "/buyVideo")
    public ResponseEntity<?> buyVideo(@RequestParam(value = "userId") String userId, @RequestParam(value = "videoId") String videoId, @RequestParam(value = "trees") BigDecimal trees, @RequestParam(value = "videoOwnerUserId") String videoOwnerUserId, HttpServletRequest request) {
        return videoPurchaseService.buyVideo(userId, videoId, trees, videoOwnerUserId);
    }

    @PostMapping(value = "/v2/buyVideo")
    public ResponseEntity<?> buyVideoV2(@RequestParam(value = "videoId") String videoId) {
        return videoPurchaseService.buyVideoV2(videoId);
    }

    @GetMapping(value = "/videoPurchaseHistory")
    public ResponseEntity<?> getVideotransactions(@RequestParam(value = "userId") String userId, HttpServletRequest request) {
        return videoPurchaseService.getVideoTransactions(userId);
    }

//    @GetMapping(value = "/treesEarned")
//    public ResponseEntity<?> getTotalTreesEarnedByVideoOwner(@RequestParam(value = "videoOwnerUserId") String videoOwnerUserId) {
//        return videoPurchaseService.getTreesEarned(videoOwnerUserId);
//    }

    @GetMapping(value = "/isVideoPurchased")
    public ResponseEntity<?> isVideoPurchasedByUser(@RequestParam(value = "userId") String userId, @RequestParam(value = "videoId") String videoId) {
        return videoPurchaseService.isVideoPurchased(userId, videoId);
    }


    @GetMapping(value = "/videoEarningAndSales")
    public ResponseEntity<?> videoEarningAndSales(@RequestParam(value = "videoIds") String videoIds) {
        return videoPurchaseService.videoEarningAndSales(videoIds);
    }

    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }

}
