package com.pding.paymentservice.controllers;

import com.pding.paymentservice.models.enums.ExposureTicketType;
import com.pding.paymentservice.payload.request.BuyExposureTicketRequest;
import com.pding.paymentservice.payload.request.ForceReleaseExposureTicketRequest;
import com.pding.paymentservice.payload.request.GiveExposureTicketByAdminRequest;
import com.pding.paymentservice.payload.request.RefundExposureTicketRequest;
import com.pding.paymentservice.payload.response.generic.GenericClassResponse;
import com.pding.paymentservice.payload.response.generic.GenericListDataResponse;
import com.pding.paymentservice.payload.response.generic.GenericPageResponse;
import com.pding.paymentservice.payload.response.generic.GenericStringResponse;
import com.pding.paymentservice.service.ExposureTicketPurchaseService;
import com.pding.paymentservice.service.ExposureTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/purchased-ticket/count")
    public ResponseEntity getListPurchasedTicketCount() {
        return ResponseEntity.ok(new GenericListDataResponse<>(null, exposureTicketPurchaseService.countUserTicketByType()));
    }

    @GetMapping("/admin/count-pd-ticket")
    public ResponseEntity getListPurchasedTicketCount(@RequestParam String pdId) {
        return ResponseEntity.ok(new GenericListDataResponse<>(null, exposureTicketPurchaseService.countUserTicketByType(pdId)));
    }

    @PostMapping("/ticket/buy")
    public ResponseEntity buyTicket(@RequestBody BuyExposureTicketRequest request) {
        return ResponseEntity.ok(new GenericClassResponse<>(null, exposureTicketPurchaseService.buyTicket(request.getType())));
    }

//    @PostMapping("/ticket/{ticketId}/use")
//    public ResponseEntity useTicket(@PathVariable String ticketId) {
//        return ResponseEntity.ok(new GenericClassResponse<>(null, exposureTicketPurchaseService.useTicket(ticketId)));
//    }

    @PostMapping("/ticket/use")
    public ResponseEntity useTicketByType(@RequestParam ExposureTicketType type) throws Exception {
        return ResponseEntity.ok(new GenericClassResponse<>(null, exposureTicketPurchaseService.useTicket(type)));
    }

    @GetMapping("/top-exposure-pds")
    public ResponseEntity getTopExposurePds() throws Exception {
        return ResponseEntity.ok(new GenericListDataResponse<>(null, exposureTicketPurchaseService.getTopExposurePds()));
    }

    @GetMapping("/admin/slots-overview")
    public ResponseEntity getSlotOverview() throws Exception {
        return ResponseEntity.ok(new GenericListDataResponse<>(null, exposureTicketPurchaseService.getSlotOverview()));
    }

    @PostMapping("/admin/force-release-ticket")
    public ResponseEntity forceReleaseTicket(@RequestBody ForceReleaseExposureTicketRequest request) throws Exception {
        exposureTicketPurchaseService.forceReleaseTicket(request.getUserId());
        return ResponseEntity.ok(new GenericStringResponse(null, "Force release ticket success"));
    }

    @PostMapping("/admin/refund-ticket")
    public ResponseEntity refundTicket(@RequestBody RefundExposureTicketRequest request) {
        exposureTicketPurchaseService.refundTicket(request.getTransactionId());
        return ResponseEntity.ok(new GenericStringResponse(null, "Refund ticket success"));
    }

    @PostMapping("admin/give-ticket")
    public ResponseEntity giveTicket(@RequestBody GiveExposureTicketByAdminRequest request) throws Exception {
        return ResponseEntity.ok(new GenericClassResponse<>(null, exposureTicketPurchaseService.giveTicket(request.getUserId(), request.getType(), request.getNumberOfTicket())));
    }
}
