package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.service.CallChargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class CallChargeServiceController {

    @Autowired
    CallChargeService callChargeService;

    @PostMapping(value = "/buyCall")
    public ResponseEntity<?> buyCall(@RequestParam(value = "userId") String userId, @RequestParam(value = "pdUserId") String pdUserId,
                                     @RequestParam(value = "leafsToCharge") BigDecimal leafsToCharge, @RequestParam(value = "callType") String callType) {
        return callChargeService.buyCall(userId, pdUserId, leafsToCharge, callType);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
