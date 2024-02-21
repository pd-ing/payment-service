package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.service.EarningService;
import com.pding.paymentservice.service.VideoPurchaseService;
import jakarta.validation.constraints.Min;
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
public class EarningServiceController {

    @Autowired
    EarningService earningService;

    @Autowired
    VideoPurchaseService videoPurchaseService;

    @GetMapping(value = "/topEarners")
    public ResponseEntity<?> allWithDrawTransactions(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                     @RequestParam(defaultValue = "10") @Min(1) int size) {
        return earningService.getTopEarners(page, size);
    }

    @GetMapping(value = "/getPurchaserOfVideo")
    public ResponseEntity<?> purchaserListOfVideo(@RequestParam String videoId) {
        return videoPurchaseService.loadPurchaseListOfSellerResponse(videoId);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
