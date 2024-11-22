package com.pding.paymentservice.controllers;

import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.service.PaymentStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment/statistic")
@RequiredArgsConstructor
public class PdStatisticController {
    private final PaymentStatisticService paymentStatisticService;
    private final AuthHelper authHelper;

    @GetMapping("/gross-revenue-graph")
    public ResponseEntity getGrossRevenueGraph(@RequestParam LocalDate date) {
        String userId = authHelper.getUserId();

        return ResponseEntity.ok(paymentStatisticService.getGrossRevenueGraph(userId, date));
    }

    @GetMapping("/gross-revenue-graph-by-date-range")
    public ResponseEntity getGrossRevenueGraph(@RequestParam LocalDate fromDate,
                                               @RequestParam LocalDate toDate) {
        String userId = authHelper.getUserId();
        return ResponseEntity.ok(paymentStatisticService.getGrossRevenueGraph(userId, fromDate, toDate));
    }
}
