package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.models.Withdrawal;
import com.pding.paymentservice.models.enums.WithdrawalStatus;
import com.pding.paymentservice.payload.request.WithdrawRequest;
import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.generic.GenericListDataResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import com.pding.paymentservice.repository.WithdrawalRepository;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.WithdrawalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
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
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class WithdrawalServiceController {

    @Autowired
    WithdrawalService withdrawalService;
    @Autowired
    WithdrawalRepository withdrawalRepository;
    @Autowired
    AuthHelper authHelper;
    @Autowired
    PdLogger pdLogger;

    @PostMapping(value = "/startWithDraw")
    public ResponseEntity<?> startWithDraw(@Valid @RequestBody WithdrawRequest withdrawRequest, BindingResult result) {
        if (result.hasErrors()) {
            // Here, we're just grabbing the first error, but you might want to send all of them.
            ObjectError error = result.getAllErrors().get(0);
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), error.getDefaultMessage())
            );
        }
        if (withdrawRequest.getTrees() == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "trees parameter is required."));
        }
        if (withdrawRequest.getLeafs() == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "leafs parameter is required."));
        }
        if (withdrawRequest.getTrees().add(withdrawRequest.getLeafs()).compareTo(BigDecimal.valueOf(500)) < 0) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Combined value of trees + leafs should be greater than 500."));
        }
        try {
            String pdUserId = authHelper.getUserId();
            withdrawalService.startWithdrawal(pdUserId, withdrawRequest.getTrees(), withdrawRequest.getLeafs());
            return ResponseEntity.ok().body(new GenericStringResponse(null, "Withdrwal process initialted successfully, Will take 5-7 businees days to credit in your account"));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.START_WITHDRAW, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @PostMapping(value = "/completeWithDraw")
    public ResponseEntity<?> completeWithDraw(@RequestBody WithdrawRequest withdrawRequest) {
        if (withdrawRequest.getPdUserId() == null || withdrawRequest.getPdUserId().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "pdUserId parameter is required."));
        }

        try {
            withdrawalService.completeWithdrawal(withdrawRequest.getPdUserId());
            return ResponseEntity.ok().body(new GenericStringResponse(null, "Withdrwal process completed successfully, Will take 5-7 businees days to credit in your account"));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.COMPLETE_WITHDRAW, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }

    }

    @PostMapping(value = "/failWithDraw")
    public ResponseEntity<?> failWithDraw(@RequestBody WithdrawRequest withdrawRequest) {
        if (withdrawRequest.getPdUserId() == null || withdrawRequest.getPdUserId().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "pdUserId parameter is required."));
        }

        try {
            withdrawalService.failWithdrawal(withdrawRequest.getPdUserId());
            return ResponseEntity.ok().body(new GenericStringResponse(null, "Withdrawal Failed, Trees and Leafs rollback done successfully"));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.COMPLETE_WITHDRAW, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }


    @GetMapping(value = "/withDrawTransactions")
    public ResponseEntity<?> withDrawTransactions(@RequestParam(value = "status", required = false) String status) {
        if (status == null || status.isEmpty()) {
            return withdrawalService.getAllWithDrawTransactionsForUserId();
        }
        WithdrawalStatus withdrawalStatus = null;
        if (status.equals("pending")) {
            withdrawalStatus = WithdrawalStatus.PENDING;
        } else if (status.equals("failed")) {
            withdrawalStatus = WithdrawalStatus.FAILED;
        } else if (status.equals("complete")) {
            withdrawalStatus = WithdrawalStatus.COMPLETE;
        } else {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid value for status parameter, Following are valid values pending, failed, complete"));
        }

        try {
            String pdUserId = authHelper.getUserId();
            List<Withdrawal> withdrawalList = withdrawalRepository.findByPdUserIdAndStatus(pdUserId, withdrawalStatus);
            return ResponseEntity.ok().body(new GenericListDataResponse<>(null, withdrawalList));
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.WITHDRAW_TRANSACTION, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericListDataResponse<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }


    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
