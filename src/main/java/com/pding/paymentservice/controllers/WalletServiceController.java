package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.WalletHistoryService;
import com.pding.paymentservice.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class WalletServiceController {

    @Autowired
    WalletService walletService;

    @Autowired
    WalletHistoryService walletHistoryService;

    @Autowired
    AuthHelper authHelper;

    @GetMapping(value = "/wallet")
    public ResponseEntity<?> getWallet(@RequestParam(value = "userId", required = false) String userId, HttpServletRequest request) {
        return walletService.getWallet(authHelper.getUserId());
    }

    @GetMapping(value = "/walletHistory")
    public ResponseEntity<?> getWalletHistory(@RequestParam(value = "userId", required = false) String userId, HttpServletRequest request) {
        return walletHistoryService.getHistory(authHelper.getUserId());
    }

    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
