package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.request.VideoPurchaseTimeRemainingRequest;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.admin.AdminDashboardUserPaymentStats;
import com.pding.paymentservice.payload.response.admin.userTabs.PaymentHistory;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.VideoPurchaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class VideoPurchaseServiceController {

    @Autowired
    AuthHelper authHelper;
    @Autowired
    VideoPurchaseService videoPurchaseService;

    @PostMapping(value = "/buyVideo")
    public ResponseEntity<?> buyVideo(@RequestParam(value = "userId", required = false) String userId, @RequestParam(value = "videoId") String videoId, @RequestParam(value = "trees") BigDecimal trees, @RequestParam(value = "videoOwnerUserId") String videoOwnerUserId, HttpServletRequest request) {
        return videoPurchaseService.buyVideo(authHelper.getUserId(), videoId, trees, videoOwnerUserId);
    }

    @PostMapping(value = "/v2/buyVideo")
    public ResponseEntity<?> buyVideoV2(@RequestParam(value = "videoId") String videoId) {
        return videoPurchaseService.buyVideoV2(videoId);
    }

    @PostMapping(value = "/v3/buyVideo")
    public ResponseEntity<?> buyVideoV3(@RequestParam(value = "videoId") String videoId, @RequestParam("duration") String duration) {
        return videoPurchaseService.buyVideoV3(videoId, duration);
    }

    @PostMapping(value = "/videoPurchaseTimeRemaining")
    public ResponseEntity<?> getVideoPurchaseTimeRemaining(@RequestBody VideoPurchaseTimeRemainingRequest request) {
        return videoPurchaseService.getVideoPurchaseTimeRemaining(request.getUserId(), request.getVideoIds());
    }

    @GetMapping(value = "/videoPurchaseHistory")
    public ResponseEntity<?> getVideotransactions(@RequestParam(value = "userId", required = false) String userId, HttpServletRequest request) {
        return videoPurchaseService.getVideoTransactions(authHelper.getUserId());
    }

    @GetMapping(value = "/videoPurchaseHistoryOfUser")
    public ResponseEntity<?> getVideoTransactionsPageable(
            @RequestParam(value = "creatorUserId", required = false) String creatorUserId,
            @RequestParam(value = "sort", defaultValue = "1") int sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int pageSize
    ) {
        return videoPurchaseService.getVideoTransactions(creatorUserId, page, pageSize, sort);
    }

//    @GetMapping(value = "/treesEarned")
//    public ResponseEntity<?> getTotalTreesEarnedByVideoOwner(@RequestParam(value = "videoOwnerUserId") String videoOwnerUserId) {
//        return videoPurchaseService.getTreesEarned(videoOwnerUserId);
//    }

    @GetMapping(value = "/isVideoPurchased")
    public ResponseEntity<?> isVideoPurchasedByUser(@RequestParam(value = "userId", required = false) String userId, @RequestParam(value = "videoId") String videoId) {
        return videoPurchaseService.isVideoPurchased(authHelper.getUserId(), videoId);
    }

    @GetMapping(value = "/v2/isVideoPurchased")
    public ResponseEntity<?> getVideoPurchasedStatus(@RequestParam(value = "videoId") String videoId) {
        return videoPurchaseService.isVideoPurchasedV2(authHelper.getUserId(), videoId);
    }

//    @GetMapping(value = "/paidUnpaidFollowerList")
//    public ResponseEntity<?> getPaidUnpaidFollowerList(@RequestParam(value = "userId", required = false) String userId)
//    {
//        return videoPurchaseService.getPaidUnpaidFollowerList(authHelper.getUserId());
//    }

    @GetMapping(value = "/paidUnpaidFollowerCount")
    public ResponseEntity<?> getPaidUnpaidFollowerCount(@RequestParam(value = "userId", required = false) String userId)
    {
        return videoPurchaseService.getPaidUnpaidFollowerCount(authHelper.getUserId());
    }


    @GetMapping(value = "/videoEarningAndSales")
    public ResponseEntity<?> videoEarningAndSales(@RequestParam(value = "videoIds") String videoIds) {
        return videoPurchaseService.videoEarningAndSales(videoIds);
    }

    @PostMapping(value = "/videoPurchaseReplacement")
    public ResponseEntity<?> videoPurchaseReplacement(
            @RequestParam(value = "videoId") String videoId,
            @RequestParam(value = "ownerUserId", required = false) String ownerUserId,
            @RequestParam(value = "userEmails") String userEmails
    ) {
        return videoPurchaseService.createVideoPurchaseReplacementFromEmail(videoId, ownerUserId, userEmails);
    }

    @GetMapping(value = "/getAllPdWhoseVideosArePurchasedByUser")
    public ResponseEntity<?> getAllPdUserIdWhoseVideosArePurchasedByUser(
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        return videoPurchaseService.getAllPdUserIdWhoseVideosArePurchasedByUser(size, page);
    }

    @GetMapping(value = "/videoSalesHistoryOfPd")
    public ResponseEntity<?> getVideoSalesHistoryOfUser(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "0") @Min(0) @Max(1) int sortOrder,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) int size
    ) {
        if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Both start date and end date should either be null or have a value"));
        }
        if (endDate != null) {
            endDate = endDate.plusDays(1L);
        }
        return videoPurchaseService.getSalesHistoryOfUser(startDate, endDate, page, size, sortOrder);
    }

    @GetMapping(value = "/searchVideoSalesHistoryOfPd")
    public ResponseEntity<?> searchVideoSalesHistoryOfUser(@RequestParam(value = "searchString") @NotBlank String searchString,
                                                           @RequestParam(defaultValue = "0") @Min(0) @Max(1) int sortOrder,
                                                           @RequestParam(defaultValue = "0") @Min(0) int page,
                                                           @RequestParam(defaultValue = "10") @Min(1) int size) {
        return videoPurchaseService.searchSalesHistoryOfUser(searchString, page, size, sortOrder);
    }

    @GetMapping(value = "/dailyTreeRevenueOfPd")
    public ResponseEntity<?> getDailyTreeRevenueOfUser(@RequestParam(value = "endDate") LocalDateTime endDate) {
        return videoPurchaseService.getDailyTreeRevenueOfUser(authHelper.getUserId(), endDate);
    }


    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }

}
