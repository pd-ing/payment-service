package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.service.EarningService;
import com.pding.paymentservice.service.PaymentService;
import com.pding.paymentservice.service.WithdrawalService;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")

public class WebhookController {

    @Value("${stripe.webhook.secret}")
    private String secretKey;

    @Autowired
    PaymentService paymentService;

    @Autowired
    PdLogger pdLogger;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String signatureHeader) {
        try {
            Event event = Webhook.constructEvent(
                    payload,
                    signatureHeader,
                    secretKey
            );
            pdLogger.logInfo("Webhook", "Callback Successfull for  " + event.getType());

            PaymentIntent paymentIntent = getPaymentIntentId(event);
            String paymentIntentId = paymentIntent.getId();
            String sessionId = paymentIntent.getMetadata().get("session_id");
            // Handle different types of events. We have configured stripe to listen to these events
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    paymentService.completePaymentToBuyTrees(paymentIntentId, sessionId);
                    break;
                case "payment_intent.payment_failed":
                    paymentService.failPaymentToBuyTrees(paymentIntentId, sessionId);
                    break;
                default:
                    pdLogger.logException(PdLogger.EVENT.STRIPE_WEBHOOK, new Exception("New event type (" + event.getType() + ") recieved in webhook, which we have not configured to listen "));
                    break;
            }
            return new ResponseEntity<>("Webhook processed successfully for the paymentIntentId:" + paymentIntentId, HttpStatus.OK);
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.STRIPE_WEBHOOK, e);
            return new ResponseEntity<>("Webhook processing failed with following exception " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private PaymentIntent getPaymentIntentId(Event event) {
        String paymentIntentId = null;
        if ("payment_intent.succeeded".equals(event.getType()) ||
                "payment_intent.payment_failed".equals(event.getType())) {

            return (PaymentIntent) event.getData().getObject();
        }
        return null;
    }
}
