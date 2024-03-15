package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.service.EarningService;
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
    WithdrawalService withdrawalService;

    @Autowired
    PdLogger pdLogger;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String signatureHeader) {
        try {
            pdLogger.logException(PdLogger.EVENT.STRIPE_WEBHOOK, new Exception("WEBHOOK" +"Callback recieved for type "+ payload));
            Event event = Webhook.constructEvent(
                    payload,
                    signatureHeader,
                    secretKey
            );
            pdLogger.logInfo("WEBHOOK","Callback Successfull for  "+ event.getType());
            // Extract Payment Intent ID
            String paymentIntentId = null;

            if ("payment_intent.succeeded".equals(event.getType()) ||
                    "payment_intent.payment_failed".equals(event.getType())) {

                PaymentIntent paymentIntent = (PaymentIntent) event.getData().getObject();
                paymentIntentId = paymentIntent.getId();
            }

            // Handle different types of events
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    //withdrawalService.completeWithdrawal(paymentIntentId);
                    break;
                case "payment_intent.payment_failed":
                    //withdrawalService.failWithdrawal(paymentIntentId);
                    break;
                default:

                    break;
            }
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.STRIPE_WEBHOOK, e);
            return new ResponseEntity<>("Webhook processing failed", HttpStatus.BAD_REQUEST);
        }
    }
}
