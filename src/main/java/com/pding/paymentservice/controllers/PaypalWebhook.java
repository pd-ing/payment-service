package com.pding.paymentservice.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/payment/paypal-webhook")
@Slf4j
public class PaypalWebhook {
    @PostMapping
    public void handlePaypalWebhook(@RequestBody String body) {
        // Handle Paypal Webhook
        log.info("Paypal Webhook received: {}", body);
    }
}
