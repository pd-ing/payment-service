package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.service.WithdrawalService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    

    @PostMapping(value = "/withDraw")
    public ResponseEntity<?> withDrawTrees(@RequestParam(value = "pdUserId") String pdUserId, @RequestParam(value = "trees") BigDecimal trees,
                                           @RequestParam(value = "transactionId") String transactionId) {
        return withdrawalService.withDraw(pdUserId, trees, transactionId);
    }

    @GetMapping(value = "/withDrawTransactions")
    public ResponseEntity<?> withDrawTrees(@RequestParam(value = "pdUserId") String pdUserId) {
        return withdrawalService.getWithDrawTransactions(pdUserId);
    }
}
