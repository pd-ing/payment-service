package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.request.AddOrRemoveTreesRequest;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import com.pding.paymentservice.payload.response.admin.AdminDashboardUserPaymentStats;
import com.pding.paymentservice.payload.response.admin.userTabs.PaymentHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.GiftHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.Status;
import com.pding.paymentservice.payload.response.admin.userTabs.ViewingHistory;
import com.pding.paymentservice.service.AdminDashboard.AdminDashboardUserPaymentStatsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
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

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment/admin")
public class AdminDashboardUserPaymentStatsController {
    @Autowired
    AdminDashboardUserPaymentStatsService adminDashboardUserPaymentStatsService;

    @PostMapping(value = "/addTrees")
    public ResponseEntity<?> addTreesFromBackend(@Valid @RequestBody AddOrRemoveTreesRequest addOrRemoveTreesRequest) {
        try {
            if (addOrRemoveTreesRequest.getTrees().compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "trees parameter should have a positive value"));
            }
            String strResponse = adminDashboardUserPaymentStatsService.addTreesFromBackend(addOrRemoveTreesRequest.getUserId(), addOrRemoveTreesRequest.getTrees());
            return ResponseEntity.ok().body(new GenericStringResponse(null, strResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @PostMapping(value = "/removeTrees")
    public ResponseEntity<?> removeTreesFromBackend(@Valid @RequestBody AddOrRemoveTreesRequest addOrRemoveTreesRequest) {
        try {
            if (addOrRemoveTreesRequest.getTrees().compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "trees parameter should have a positive value"));
            }
            String strResponse = adminDashboardUserPaymentStatsService.removeTreesFromBackend(addOrRemoveTreesRequest.getUserId(), addOrRemoveTreesRequest.getTrees());
            return ResponseEntity.ok().body(new GenericStringResponse(null, strResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }


    @GetMapping(value = "/statusTab")
    public ResponseEntity<?> getStatusTabDetailsController(@RequestParam(value = "userId") String userId) {
        Status status = null;
        try {
            status = adminDashboardUserPaymentStatsService.getStatusTabDetails(userId);
            return ResponseEntity.ok(new AdminDashboardUserPaymentStats(null, status));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AdminDashboardUserPaymentStats(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), status));
        }
    }


    @GetMapping(value = "/viewingHistoryTab")
    public ResponseEntity<?> getViewingHistoryTabDetailsController(@RequestParam(value = "userId") String userId, @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                   @RequestParam(defaultValue = "10") @Min(1) int size) {
        ViewingHistory viewingHistory = null;
        try {
            viewingHistory = adminDashboardUserPaymentStatsService.getViewingHistory(userId, page, size);
            return ResponseEntity.ok(new AdminDashboardUserPaymentStats(null, viewingHistory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AdminDashboardUserPaymentStats(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), viewingHistory));
        }
    }

    @GetMapping(value = "/viewingHistoryTabSearchVideo")
    public ResponseEntity<?> getViewingHistoryTabDetailsSearchVideoController(@RequestParam(value = "userId") @NotBlank String userId,
                                                                              @RequestParam(value = "videoTitle") @NotBlank String videoTitle,
                                                                              @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                              @RequestParam(defaultValue = "10") @Min(1) int size) {
        ViewingHistory viewingHistory = null;
        try {
            viewingHistory = adminDashboardUserPaymentStatsService.searchVideo(userId, videoTitle, page, size);
            return ResponseEntity.ok(new AdminDashboardUserPaymentStats(null, viewingHistory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AdminDashboardUserPaymentStats(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), viewingHistory));
        }
    }

    @GetMapping(value = "/paymentHistoryTab")
    public ResponseEntity<?> getPaymentHistoryTabDetailsController(@RequestParam(value = "userId") String userId, @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                   @RequestParam(defaultValue = "10") @Min(1) int size) {
        PaymentHistory paymentHistory = null;
        try {
            paymentHistory = adminDashboardUserPaymentStatsService.getPaymentHistory(userId, page, size);
            return ResponseEntity.ok(new AdminDashboardUserPaymentStats(null, paymentHistory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AdminDashboardUserPaymentStats(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), paymentHistory));
        }
    }

    @GetMapping(value = "/giftHistoryTab")
    public ResponseEntity<?> getGiftHistoryTabDetailsController(@RequestParam(value = "userId") String userId, @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                @RequestParam(defaultValue = "10") @Min(1) int size) {
        GiftHistory giftHistory = null;
        try {
            giftHistory = adminDashboardUserPaymentStatsService.getGiftHistoryTabDetails(userId, page, size);
            return ResponseEntity.ok(new AdminDashboardUserPaymentStats(null, giftHistory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AdminDashboardUserPaymentStats(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), giftHistory));
        }
    }

    @GetMapping(value = "/paymentHistoryAllUsersTab")
    public ResponseEntity<?> getPaymentHistoryForAllUsersTabDetailsController(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                                                              @RequestParam(defaultValue = "0") @Min(0) @Max(1) int sortOrder,
                                                                              @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                              @RequestParam(defaultValue = "10") @Min(1) @Max(10) int size) {
        PaymentHistory paymentHistory = null;
        try {
            if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Both start date and end date should either be null or have a value"));
            }
            paymentHistory = adminDashboardUserPaymentStatsService.getPaymentHistoryForAllUsers(startDate, endDate, sortOrder, page, size);
            return ResponseEntity.ok(new AdminDashboardUserPaymentStats(null, paymentHistory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AdminDashboardUserPaymentStats(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), paymentHistory));
        }
    }

    @GetMapping(value = "/paymentHistoryAllUsersSearchByEmail")
    public ResponseEntity<?> getPaymentHistoryAllUsersTabDetailsSearchEmailController(@RequestParam(value = "searchString") @NotBlank String searchString,
                                                                                      @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                                      @RequestParam(defaultValue = "10") @Min(1) int size) {
        PaymentHistory paymentHistory = null;
        try {
            paymentHistory = adminDashboardUserPaymentStatsService.searchPaymentHistoryByEmail(searchString, page, size);
            return ResponseEntity.ok(new AdminDashboardUserPaymentStats(null, paymentHistory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AdminDashboardUserPaymentStats(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), paymentHistory));
        }
    }

    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
