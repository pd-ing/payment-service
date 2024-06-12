package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.request.AddOrRemoveTreesRequest;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.TreeSummary;
import com.pding.paymentservice.payload.response.admin.TreeSummaryGridResult;
import com.pding.paymentservice.payload.response.admin.userTabs.*;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.ReferralCommissionHistory;
import com.pding.paymentservice.payload.response.admin.userTabs.entitesForAdminDasboard.ReferredPdDetails;
import com.pding.paymentservice.payload.response.generic.GenericPageResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import com.pding.paymentservice.payload.response.admin.AdminDashboardUserPaymentStats;
import com.pding.paymentservice.payload.response.referralTab.ReferredPDDetailsRecord;
import com.pding.paymentservice.payload.response.referralTab.ReferredPDWithdrawalRecord;
import com.pding.paymentservice.payload.response.referralTab.ReferrerPDDetailsRecord;
import com.pding.paymentservice.service.AdminDashboard.AdminDashboardUserPaymentStatsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

    @GetMapping(value = "/statusTabForPd")
    public ResponseEntity<?> getStatusTabForPdDetailsController(@RequestParam(value = "pdUserId") String pdUserId) {
        StatusForPd statusPd = null;
        try {
            statusPd = adminDashboardUserPaymentStatsService.getStatusTabForPdDetails(pdUserId);
            return ResponseEntity.ok(new AdminDashboardUserPaymentStats(null, statusPd));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AdminDashboardUserPaymentStats(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), statusPd));
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

    @GetMapping(value = "/viewingHistoryTabForPd")
    public ResponseEntity<?> getViewingHistoryTabForPdDetailsController(@RequestParam(value = "pdUserId") String pdUserId, @RequestParam(required = false) String searchString,
                                                                        @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                        @RequestParam(defaultValue = "10") @Min(1) int size) {
        ViewingHistoryForPd viewingHistoryForPd = null;
        try {
            viewingHistoryForPd = adminDashboardUserPaymentStatsService.getViewingHistoryForPd(pdUserId, searchString, page, size);
            return ResponseEntity.ok(new AdminDashboardUserPaymentStats(null, viewingHistoryForPd));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AdminDashboardUserPaymentStats(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), viewingHistoryForPd));
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

    @GetMapping(value = "/giftHistoryTabForPd")
    public ResponseEntity<?> getGiftHistoryTabDetailsController(@RequestParam(value = "pdUserId") String pdUserId,
                                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                                                @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                @RequestParam(defaultValue = "10") @Min(1) int size) {
        GiftHistoryForPd giftHistoryForPd = null;
        try {
            if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Both start date and end date should either be null or have a value"));
            }
            if (endDate != null) {
                endDate = endDate.plusDays(1L);
            }
            giftHistoryForPd = adminDashboardUserPaymentStatsService.getGiftHistoryTabForPdDetails(pdUserId, startDate, endDate, page, size);
            return ResponseEntity.ok(new AdminDashboardUserPaymentStats(null, giftHistoryForPd));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AdminDashboardUserPaymentStats(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), giftHistoryForPd));
        }
    }

    @GetMapping(value = "/withdrawalHistoryTabForPd")
    public ResponseEntity<?> getGiftHistoryTabDetailsController(@RequestParam(value = "pdUserId") String pdUserId,
                                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                                                @RequestParam(defaultValue = "0") @Min(0) @Max(1) int sortOrder,
                                                                @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                @RequestParam(defaultValue = "10") @Min(1) int size) {
        WithdrawHistoryForPd withdrawHistoryForPd = null;
        try {
            if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Both start date and end date should either be null or have a value"));
            }
            if (endDate != null) {
                endDate = endDate.plusDays(1L);
            }
            withdrawHistoryForPd = adminDashboardUserPaymentStatsService.getWithdrawHistoryTabForPdDetails(pdUserId, startDate, endDate, sortOrder, page, size);
            return ResponseEntity.ok(new AdminDashboardUserPaymentStats(null, withdrawHistoryForPd));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AdminDashboardUserPaymentStats(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), withdrawHistoryForPd));
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
            if (endDate != null) {
                endDate = endDate.plusDays(1L);
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

    @GetMapping(value = "/treeSummariesAllPd")
    public ResponseEntity<?> getTreeSummaryByUserTabDetailsController(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                                      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                                                      @RequestParam(required = false) String searchString,
                                                                      @RequestParam(defaultValue = "0") @Min(0) @Max(1) int sortOrder,
                                                                      @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "10") @Min(1) int size) {
        TreeSummaryGridResult treeSummaryGridResult = null;
        try {
            if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Both start date and end date should either be null or have a value"));
            }
            // Doing 1 more day in endDate as it takes 12am that is start if the endDate
            if (endDate != null) {
                endDate = endDate.plusDays(1L);
            }
            treeSummaryGridResult = adminDashboardUserPaymentStatsService.getTreesSummaryForAllUsers(startDate, endDate, searchString, page, size);
            return ResponseEntity.ok(new AdminDashboardUserPaymentStats(null, treeSummaryGridResult));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AdminDashboardUserPaymentStats(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), treeSummaryGridResult));
        }
    }

    @GetMapping(value = "/treeSummariesTotals")
    public ResponseEntity<?> getTreeSummaryDetailsController(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                                             @RequestParam(required = false) String searchString) {
        TreeSummary treesSummaryTotals = null;
        try {
            if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Both start date and end date should either be null or have a value"));
            }
            // Doing 1 more day in endDate as it takes 12am that is start if the endDate
            if (endDate != null) {
                endDate = endDate.plusDays(1L);
            }
            treesSummaryTotals = adminDashboardUserPaymentStatsService.getTreesSummaryTotals(startDate, endDate, searchString);
            return ResponseEntity.ok(new AdminDashboardUserPaymentStats(null, treesSummaryTotals));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AdminDashboardUserPaymentStats(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), treesSummaryTotals));
        }
    }

    @GetMapping(value = "/realTimeTreeUsageHistory")
    public ResponseEntity<?> getRealTimeTreeUsageHistoryDetailsController(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                                                          @RequestParam(required = false) String searchString,
                                                                          @RequestParam(defaultValue = "0") @Min(0) @Max(1) int sortOrder,
                                                                          @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "10") @Min(1) int size) {
        RealTimeTreeTransactionHistory realTimeTransactionHistory = null;
        try {
            if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Both start date and end date should either be null or have a value"));
            }
            // Doing 1 more day in endDate as it takes 12am that is start if the endDate
            if (endDate != null) {
                endDate = endDate.plusDays(1L);
            }
            realTimeTransactionHistory = adminDashboardUserPaymentStatsService.getRealTimeTreeUsage(startDate, endDate, searchString, page, size);
            return ResponseEntity.ok(new AdminDashboardUserPaymentStats(null, realTimeTransactionHistory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AdminDashboardUserPaymentStats(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), realTimeTransactionHistory));
        }
    }

    @GetMapping(value = "/realTimeTreeUsageTotals")
    public ResponseEntity<?> getRealTimeTreeUsageTotalsDetailsController(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        TotalTreeUsageSummary totalTreeUsageSummary = null;
        try {
            if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Both start date and end date should either be null or have a value"));
            }
            // Doing 1 more day in endDate as it takes 12am that is start if the endDate
            if (endDate != null) {
                endDate = endDate.plusDays(1L);
            }
            totalTreeUsageSummary = adminDashboardUserPaymentStatsService.getTotalTreeUsageSummary(startDate, endDate);
            return ResponseEntity.ok(new AdminDashboardUserPaymentStats(null, totalTreeUsageSummary));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AdminDashboardUserPaymentStats(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), totalTreeUsageSummary));
        }
    }

    @GetMapping(value = "/referenceTabDetails")
    public ResponseEntity<?> getReferenceTabDetails(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                    @RequestParam(defaultValue = "10") @Min(1) int size,
                                                    @RequestParam(required = false) String searchString) {
        try {
            Page<ReferralCommissionHistory> referralCommissionHistoryPage =
                    adminDashboardUserPaymentStatsService.getReferenceTabDetails(page, size, searchString);
            return ResponseEntity.ok(new GenericPageResponse<>(null, referralCommissionHistoryPage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericPageResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @GetMapping(value = "/modalForReferenceTab")
    public ResponseEntity<?> getModalForReferenceTab(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                     @RequestParam(defaultValue = "10") @Min(1) int size,
                                                     @RequestParam(required = false) String referrerPdUserId) {

        if (referrerPdUserId == null || referrerPdUserId.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "referrerPdUserId parameter cannot be empty"));
        }
        try {
            Page<ReferredPdDetails> referredPdDetails =
                    adminDashboardUserPaymentStatsService.getReferredPdDetails(referrerPdUserId, page, size);
            return ResponseEntity.ok(new GenericPageResponse<>(null, referredPdDetails));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericPageResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    // PD Settlement Details tab (FE)
    @GetMapping("/listReferredPdsEOL")
    ResponseEntity<?> listReferredPdDetailsEOL(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                            @RequestParam(required = false) String searchString,
                                            @RequestParam(defaultValue = "0") @Min(0) int page,
                                            @RequestParam(defaultValue = "10") @Min(1) int size,
                                            @RequestParam String referrerPdUserId) {

        try {
            if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Both start date and end date should either be null or have a value"));
            }
            // Doing 1 more day in endDate as it takes 12am that is start if the endDate
            if (endDate != null) {
                endDate = endDate.plusDays(1L);
            }
            Page<ReferredPDDetailsRecord> referredPDDetailsRecords = adminDashboardUserPaymentStatsService.listReferredPdDetailsEOL(referrerPdUserId, startDate, endDate, searchString, page, size);
            return ResponseEntity.ok().body(new GenericPageResponse<>(null, referredPDDetailsRecords));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericPageResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @GetMapping("/listReferrerPds")
    ResponseEntity<?> listReferrerPdDetails(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                            @RequestParam(required = false) String searchString,
                                            @RequestParam(defaultValue = "0") @Min(0) int page,
                                            @RequestParam(defaultValue = "10") @Min(1) int size,
                                            @RequestParam(required = false) String referredPdUserId) {

        try {
            if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Both start date and end date should either be null or have a value"));
            }
            // Doing 1 more day in endDate as it takes 12am that is start if the endDate
            if (endDate != null) {
                endDate = endDate.plusDays(1L);
            }
            Page<ReferrerPDDetailsRecord> referredPDDetailsRecords = adminDashboardUserPaymentStatsService.listReferrerPdDetails(referredPdUserId, startDate, endDate, searchString, page, size);
            return ResponseEntity.ok().body(new GenericPageResponse<>(null, referredPDDetailsRecords));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericPageResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @GetMapping("/listReferredPds")
    ResponseEntity<?> getWithdrawalHistoryForReferredPds(@RequestParam(value = "referrerPdUserId") String referrerPdUserId, @RequestParam(defaultValue = "0") @Min(0) int page,
                                                         @RequestParam(defaultValue = "10") @Min(1) int size) {

        try {
            // String referrerPdUserId = authHelper.getUserId();
            Page<ReferredPDDetailsRecord> referredPDWithdrawalRecords = adminDashboardUserPaymentStatsService.listReferredPdDetails(referrerPdUserId, page, size);
            return ResponseEntity.ok().body(new GenericPageResponse<>(null, referredPDWithdrawalRecords));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericPageResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }


    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
