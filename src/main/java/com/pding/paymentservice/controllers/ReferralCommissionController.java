package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.payload.request.ReferralCommissionRequest;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import com.pding.paymentservice.service.ReferralCommissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class ReferralCommissionController {

    @Autowired
    ReferralCommissionService referralCommissionService;

    @Autowired
    PdLogger pdLogger;

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
}
