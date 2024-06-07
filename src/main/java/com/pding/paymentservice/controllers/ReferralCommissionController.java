package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.other.services.tables.dto.ReferralCommissionDetailsDTO;
import com.pding.paymentservice.models.other.services.tables.dto.ReferredPdDetailsDTO;
import com.pding.paymentservice.payload.request.ReferralCommissionRequest;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.admin.AdminDashboardUserPaymentStats;
import com.pding.paymentservice.payload.response.generic.GenericPageResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import com.pding.paymentservice.payload.response.referralTab.ReferredPDDetailsRecord;
import com.pding.paymentservice.payload.response.referralTab.ReferredPDWithdrawalRecord;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.ReferralCommissionService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class ReferralCommissionController {

    @Autowired
    ReferralCommissionService referralCommissionService;

    @Autowired
    PdLogger pdLogger;

    @Autowired
    AuthHelper authHelper;

    @PostMapping("/completeReferralCommission")
    ResponseEntity<?> completeReferralCommission(@RequestBody ReferralCommissionRequest referralCommissionRequest) {
        if (referralCommissionRequest.getReferralCommissionId() == null || referralCommissionRequest.getReferralCommissionId().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "trees parameter is required."));
        }
        try {
            String message = referralCommissionService.updateReferralCommissionEntryToCompletedState(referralCommissionRequest.getReferralCommissionId());
            return ResponseEntity.ok().body(new GenericStringResponse(null, message));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.COMPLETE_REFERRAL_COMMISSION, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    // PD Registration status tab (FE)
    @GetMapping("/getDetailsOfAllTheReferredPd")
    ResponseEntity<?> getDetailsOfAllTheReferredPd(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                   @RequestParam(defaultValue = "10") @Min(1) int size) {

        try {
            String referrerPdUserId = authHelper.getUserId();
            Page<ReferredPdDetailsDTO> referredPdDetailsDTOPage = referralCommissionService.getDetailsOfAllTheReferredPd(referrerPdUserId, page, size);
            return ResponseEntity.ok().body(new GenericPageResponse<>(null, referredPdDetailsDTOPage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericPageResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    // PD Settlement Details tab (FE)
    @GetMapping("/listReferredPds")
    ResponseEntity<?> listReferredPdDetails(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                            @RequestParam(required = false) String searchString,
                                            @RequestParam(defaultValue = "0") @Min(0) int page,
                                            @RequestParam(defaultValue = "10") @Min(1) int size) {

        try {
            String referrerPdUserId = authHelper.getUserId();
            if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Both start date and end date should either be null or have a value"));
            }
            // Doing 1 more day in endDate as it takes 12am that is start if the endDate
            if (endDate != null) {
                endDate = endDate.plusDays(1L);
            }
            Page<ReferredPDDetailsRecord> referredPDDetailsRecords = referralCommissionService.listReferredPdDetails(referrerPdUserId, startDate, endDate, searchString, page, size);
            return ResponseEntity.ok().body(new GenericPageResponse<>(null, referredPDDetailsRecords));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericPageResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @GetMapping("/getWithdrawalHistoryForReferredPds")
    ResponseEntity<?> getWithdrawalHistoryForReferredPds(@RequestParam(value = "pdUserId") String pdUserId, @RequestParam(defaultValue = "0") @Min(0) int page,
                                                         @RequestParam(defaultValue = "10") @Min(1) int size) {

        try {
            // String referrerPdUserId = authHelper.getUserId();
            Page<ReferredPDWithdrawalRecord> referredPDWithdrawalRecords = referralCommissionService.getWithdrawalHistoryForReferredPds(pdUserId, page, size);
            return ResponseEntity.ok().body(new GenericPageResponse<>(null, referredPDWithdrawalRecords));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericPageResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    // PD Settlement Details tab (FE)
    @GetMapping("/getReferralCommissionDetailsWithFilters")
    ResponseEntity<?> getReferralCommissionDetailsWithFilters(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                              @RequestParam(defaultValue = "10") @Min(1) int size,
                                                              @RequestParam(required = false) String searchString,
                                                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        try {
            if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Both start date and end date should either be null or have a value"));
            }
            // Doing 1 more day in endDate as it takes 12am that is start if the endDate
            if (endDate != null) {
                endDate = endDate.plusDays(1L);
            }
            String referrerPdUserId = authHelper.getUserId();
            Page<ReferralCommissionDetailsDTO> referralCommissionDetailsDTOPage = referralCommissionService.getReferralCommissionDetailsWithFilters(
                    referrerPdUserId,
                    page,
                    size,
                    searchString,
                    startDate,
                    endDate
            );
            return ResponseEntity.ok(new GenericPageResponse<>(null, referralCommissionDetailsDTOPage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericPageResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }


}
