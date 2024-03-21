package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.response.ErrorResponse;
import com.pding.paymentservice.payload.response.GenericListDataResponse;
import com.pding.paymentservice.payload.response.GenericStringResponse;
import com.pding.paymentservice.payload.response.WalletHistoryResponse;
import com.pding.paymentservice.service.AdminService;
import com.pding.paymentservice.service.PaymentService;
import com.pding.paymentservice.service.WithdrawalService;
import jakarta.validation.constraints.Min;
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
public class AdminController {
    @Autowired
    WithdrawalService withdrawalService;


    @Autowired
    PaymentService paymentService;
    @Autowired
    AdminService adminService;

    @GetMapping(value = "/admin/allWithDrawTransactions")
    public ResponseEntity<?> allWithDrawTransactions(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                     @RequestParam(defaultValue = "10") @Min(1) int size) {
        return withdrawalService.getAllWithDrawTransactions(page, size);
    }

    @GetMapping(value = "/admin/pendingWithDrawTransactions")
    public ResponseEntity<?> pendingWithDrawTransactions() {
        return withdrawalService.getPendingWithDrawTransactions();
    }

    @GetMapping(value = "/admin/balanceTrees")
    public ResponseEntity<?> balanceTrees() {
        return adminService.balanceTrees();
    }

    @PostMapping(value = "/admin/addTrees")
    public ResponseEntity<?> addTreesFromBackend(@RequestParam(value = "userId") String userId, @RequestParam(value = "trees") BigDecimal trees) {
        try {
            if (trees.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "trees parameter should have a positive value"));
            }
            String strResponse = adminService.addTreesFromBackend(userId, trees);
            return ResponseEntity.ok().body(new GenericStringResponse(null, strResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    @PostMapping(value = "/admin/removeTrees")
    public ResponseEntity<?> removeTreesFromBackend(@RequestParam(value = "userId") String userId, @RequestParam(value = "trees") BigDecimal trees) {
        try {
            if (trees.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "trees parameter should have a positive value"));
            }
            String strResponse = adminService.removeTreesFromBackend(userId, trees);
            return ResponseEntity.ok().body(new GenericStringResponse(null, strResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericStringResponse(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), null));
        }
    }

    // Handle MissingServletRequestParameterException --
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Required request parameter '" + paramName + "' is missing or invalid."));
    }
}
