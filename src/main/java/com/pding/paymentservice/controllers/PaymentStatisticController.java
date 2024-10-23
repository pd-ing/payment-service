package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.dto.PdSummaryDTO;
import com.pding.paymentservice.payload.response.generic.GenericClassResponse;
import com.pding.paymentservice.payload.response.generic.GenericPageResponse;
import com.pding.paymentservice.service.PaymentStatisticService;
import com.pding.paymentservice.service.VideoPurchaseService;
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
    private final VideoPurchaseService videoPurchaseService;

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

    @GetMapping("/leafPaymentHistory")
    public ResponseEntity leafPaymentHistory(
                                            @RequestParam(required = false) String startDate,
                                            @RequestParam(required = false) String endDate,
                                            @RequestParam(required = false) String searchString,
                                            Pageable pageable) {
        return ResponseEntity.ok(new GenericPageResponse<>(null, paymentStatisticService.leafPaymentHistory(searchString, startDate, endDate, pageable)));
    }

    @GetMapping("/leafPaymentHistorySummary")
    public ResponseEntity leafPaymentHistorySummary() {
        return ResponseEntity.ok(new GenericClassResponse<>(null, paymentStatisticService.leafPaymentHistorySummary()));
    }

    @GetMapping("/videoSaleHistorySummary")
    public ResponseEntity videoSaleHistorySummary(@RequestParam("videoId") String videoId) {
        return ResponseEntity.ok(new GenericClassResponse<>(null, videoPurchaseService.getVideoSaleSummary(videoId)));
    }

    @GetMapping("/videoSaleHistory")
    public ResponseEntity videoSaleHistory(@RequestParam("videoId") String videoId, Pageable pageable) {
        return ResponseEntity.ok(new GenericPageResponse<>(null, videoPurchaseService.getVideoPurchaseHistory(videoId, pageable)));
    }
}
