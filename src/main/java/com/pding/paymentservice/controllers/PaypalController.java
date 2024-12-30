package com.pding.paymentservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pding.paymentservice.payload.request.CreatePaypalOrderRequest;
import com.pding.paymentservice.service.PaypalService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("api/payment/paypal")
public class PaypalController {

    @Autowired
    PaypalService paypalService;

    @PostMapping("/webhook")
    public void handlePaypalWebhook(@RequestBody String body) {
        System.out.println("Paypal webhook received: " + body);
    }

    @PostMapping("/createOrder")
    public ResponseEntity createOrder(@RequestBody CreatePaypalOrderRequest body) throws StripeException, JsonProcessingException {
        String priceId = body.getPriceId();
        return ResponseEntity.ok(paypalService.createOrder(priceId));
    }

    @PostMapping("/captureOrder")
    public ResponseEntity captureOrder(@RequestBody CreatePaypalOrderRequest body) throws StripeException, JsonProcessingException {
        String orderId = body.getOrderId();
        return ResponseEntity.ok(paypalService.captureOrder(orderId));
    }
}
