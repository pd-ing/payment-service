package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.dto.PdSummaryDTO;
import com.pding.paymentservice.payload.response.generic.GenericClassResponse;
import com.pding.paymentservice.payload.response.generic.GenericPageResponse;
import com.pding.paymentservice.service.PaymentStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment/statistic")
@RequiredArgsConstructor
public class PaymentStatisticController {
    private final PaymentStatisticService paymentStatisticService;

    @GetMapping(value = "/leafsEarningFromCallHistory")
    public ResponseEntity leafsEarningHistory(@RequestParam String pdId,
                                              @RequestParam(required = false) String startDate,
                                              @RequestParam(required = false) String endDate,
                                              Pageable pageable) {
        return ResponseEntity.ok(new GenericPageResponse<>(null, paymentStatisticService.leafsEarningHistory(pdId, startDate, endDate, pageable)));
    }

    @GetMapping(value = "/leafsEarningFromGiftHistory")
    public ResponseEntity leafsEarningFromGiftHistory(@RequestParam String pdId,
                                                      Pageable pageable) {
        return ResponseEntity.ok(new GenericPageResponse<>(null, paymentStatisticService.leafsEarningFromGiftHistory(pdId, pageable)));
    }

    @GetMapping("/pdSummary")
    public ResponseEntity<GenericClassResponse<PdSummaryDTO>> pdSummary(@RequestParam String pdId) {
        return ResponseEntity.ok(new GenericClassResponse<>(null, paymentStatisticService.pdSummary(pdId).block()));
    }
}
