package com.pding.paymentservice.controllers;

import com.pding.paymentservice.models.ExposureTicketPurchase;
import com.pding.paymentservice.payload.request.BuyExposureTicketRequest;
import com.pding.paymentservice.payload.response.generic.GenericClassResponse;
import com.pding.paymentservice.payload.response.generic.GenericListDataResponse;
import com.pding.paymentservice.payload.response.generic.GenericPageResponse;
import com.pding.paymentservice.service.ExposureTicketPurchaseService;
import com.pding.paymentservice.service.ExposureTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class ExposureTicketController {
    private final ExposureTicketService exposureTicketService;
    private final ExposureTicketPurchaseService exposureTicketPurchaseService;

    @GetMapping("/ticket-prices")
    public ResponseEntity getListExposureTicket() {
        return ResponseEntity.ok(new GenericListDataResponse<>(null, exposureTicketService.getTicketPrices()));
    }

    @GetMapping("/purchased-ticket")
    public ResponseEntity getListPurchasedTicket(Pageable pageable) {
        return ResponseEntity.ok(new GenericPageResponse<>(null, exposureTicketPurchaseService.getPurchasedTicketOfUser(pageable)));
    }

    @PostMapping("/ticket/buy")
    public ResponseEntity buyTicket(@RequestBody BuyExposureTicketRequest request) {
        return ResponseEntity.ok(new GenericClassResponse<>(null, exposureTicketPurchaseService.buyTicket(request.getType())));
    }

    @PostMapping("/ticket/{ticketId}/use")
    public ResponseEntity useTicket(@PathVariable String ticketId) {
        return ResponseEntity.ok(new GenericClassResponse<>(null, exposureTicketPurchaseService.useTicket(ticketId)));
    }

}
