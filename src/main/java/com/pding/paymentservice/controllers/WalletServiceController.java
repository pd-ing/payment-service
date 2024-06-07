package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.WalletHistory;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.WalletHistoryResponse;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.WalletHistoryService;
import com.pding.paymentservice.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @Autowired
    PdLogger pdLogger;

    @GetMapping(value = "/wallet")
    public ResponseEntity<?> getWallet(@RequestParam(value = "userId", required = false) String userId, HttpServletRequest request) {
        return walletService.getWallet(authHelper.getUserId());
    }

    @GetMapping(value = "/walletHistory")
    public ResponseEntity<?> getWalletHistory(@RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number must be greater than or equal to 0") int page,
                                              @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than or equal to 1") int size) {
        try {
            String userId = authHelper.getUserId();
            Page<WalletHistory> walletHistory = walletHistoryService.fetchWalletHistoryByUserId(userId, page, size);
            return ResponseEntity.ok().body(new WalletHistoryResponse(null, walletHistory));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.WALLET_HISTORY, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new WalletHistoryResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @GetMapping(value = "/leafsWalletHistory")
    public ResponseEntity<?> getPurchasedLeafsWalletHistory(@RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number must be greater than or equal to 0") int page,
                                                            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than or equal to 1") int size,
                                                            @RequestParam(value = "sortAsc", defaultValue = "false", required = false) Boolean sortAsc) {
        try {
            String userId = authHelper.getUserId();
            Page<WalletHistory> walletHistory = walletHistoryService.fetchPurchasedLeafWalletHistoryByUserId(userId, page, size, sortAsc);
            return ResponseEntity.ok().body(new WalletHistoryResponse(null, walletHistory));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.WALLET_HISTORY, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new WalletHistoryResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
