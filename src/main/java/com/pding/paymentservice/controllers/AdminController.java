package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.service.AdminService;
import com.pding.paymentservice.service.PaymentService;
import com.pding.paymentservice.service.WithdrawalService;
import jakarta.validation.constraints.Max;
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
public class AdminController {
    @Autowired
    WithdrawalService withdrawalService;


    @Autowired
    PaymentService paymentService;
    @Autowired
    AdminService adminService;

    @GetMapping(value = "/admin/allWithDrawTransactions")
    public ResponseEntity<?> allWithDrawTransactions(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                     @RequestParam(defaultValue = "10") @Min(1) int size,
                                                     @RequestParam(defaultValue = "0") @Min(0) @Max(1) int sortOrder,
                                                     @RequestParam(value = "searchString" ,required = false) String searchString
                                                     ) {
        return withdrawalService.getAllWithDrawTransactions(page, size, sortOrder, searchString);
    }

    @GetMapping(value = "/admin/pendingWithDrawTransactions")
    public ResponseEntity<?> pendingWithDrawTransactions() {
        return withdrawalService.getPendingWithDrawTransactions();
    }

    @GetMapping(value = "/admin/balanceTrees")
    public ResponseEntity<?> balanceTrees() {
        return adminService.balanceTrees();
    }


    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
