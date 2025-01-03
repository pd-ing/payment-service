package com.pding.paymentservice.controllers;

import com.pding.paymentservice.payload.response.generic.GenericListDataResponse;
import com.pding.paymentservice.service.ExposureTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class ExposureTicketController {
    private final ExposureTicketService exposureTicketService;

    @GetMapping("/exposure-ticket")
    public ResponseEntity getListExposureTicket() {
        return ResponseEntity.ok(new GenericListDataResponse<>(null, exposureTicketService.getTicketPrices()));
    }


}
