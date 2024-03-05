package com.pding.paymentservice.controllers;

import com.pding.paymentservice.models.Earning;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.DonationService;
import com.pding.paymentservice.service.EarningService;
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
public class DonationServiceController {

    @Autowired
    AuthHelper authHelper;
    @Autowired
    DonationService donationService;

    @PostMapping(value = "/donate")
    public ResponseEntity<?> donateTrees(@RequestParam(value = "donorUserId", required = false) String donorUserId, @RequestParam(value = "trees") BigDecimal trees, @RequestParam(value = "pdUserId") String pdUserId) {
        return donationService.donateToPd(authHelper.getUserId(), trees, pdUserId);
    }

    @PostMapping(value = "/v2/donate")
    public ResponseEntity<?> donateTreesV2(@RequestParam(value = "trees") BigDecimal trees, @RequestParam(value = "pdUserId") String pdUserId) {
        return donationService.donateToPdV2(trees, pdUserId);
    }

    @GetMapping(value = "/donationHistoryForUser")
    public ResponseEntity<?> getDonationHistoryForUser(@RequestParam(value = "donorUserId", required = false) String donorUserId) {
        return donationService.getDonationHistoryForUser(authHelper.getUserId());
    }

    @GetMapping(value = "/donationHistoryForPd")
    public ResponseEntity<?> getDonationHistoryForPd(@RequestParam(value = "pdUserId") String pdUserId) {
        return donationService.getDonationHistoryForPd(pdUserId);
    }

    @GetMapping(value = "/topDonorsList")
    public ResponseEntity<?> getDonationHistoryForPd(@RequestParam(value = "pdUserId") String pdUserId, @RequestParam(value = "limit") Long limit) {
        return donationService.getTopDonors(pdUserId, limit);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
