package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.payload.request.WithdrawRequest;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.service.WithdrawalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class WithdrawalController {

    @Autowired
    WithdrawalService withdrawalService;


    @PostMapping(value = "/startWithDraw")
    public ResponseEntity<?> startWithDraw(@RequestBody WithdrawRequest withdrawRequest) {
        return withdrawalService.startWithDrawal(withdrawRequest);
    }

    @PostMapping(value = "/completeWithDraw")
    public ResponseEntity<?> completeWithDraw(@RequestBody WithdrawRequest withdrawRequest) {
        return withdrawalService.completeWithDraw(withdrawRequest);
    }

    @PostMapping(value = "/failWithDraw")
    public ResponseEntity<?> failWithDraw(@RequestBody WithdrawRequest withdrawRequest) {
        return withdrawalService.failWithDraw(withdrawRequest);
    }


    @GetMapping(value = "/withDrawTransactions")
    public ResponseEntity<?> withDrawTransactions(@RequestParam(value = "status", required = false) String status) {
        return withdrawalService.getWithDrawTransactions(status);
    }

    @GetMapping(value = "/admin/pendingWithDrawTransactions")
    public ResponseEntity<?> pendingWithDrawTransactions() {
        return withdrawalService.getPendingWithDrawTransactions();
    }

    @GetMapping(value = "/admin/allWithDrawTransactions")
    public ResponseEntity<?> allWithDrawTransactions(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                     @RequestParam(defaultValue = "10") @Min(1) int size) {
        return withdrawalService.getAllWithDrawTransactions(page, size);
    }

    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
