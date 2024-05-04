package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.other.services.tables.dto.ReferredPdDetailsDTO;
import com.pding.paymentservice.payload.request.ReferralCommissionRequest;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.generic.GenericPageResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.ReferralCommissionService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/getDetailsOfAllTheReferredPd")
    ResponseEntity<?> getDetailsOfAllTheReferredPd(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                   @RequestParam(defaultValue = "10") @Min(1) int size) {

        try {
            String referrerPdUserId = authHelper.getUserId();
            referrerPdUserId = "F6ZjgjPCwAUKSdCl0UgDwEMD0q52";
            Page<ReferredPdDetailsDTO> referredPdDetailsDTOPage = referralCommissionService.getDetailsOfAllTheReferredPd(referrerPdUserId, page, size);
            return ResponseEntity.ok().body(new GenericPageResponse<>(null, referredPdDetailsDTOPage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericPageResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }

    }
}
