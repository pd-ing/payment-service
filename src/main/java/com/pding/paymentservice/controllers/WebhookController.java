package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.service.EarningService;
import com.pding.paymentservice.service.PaymentService;
import com.pding.paymentservice.service.WalletHistoryService;
import com.pding.paymentservice.service.WithdrawalService;
import com.pding.paymentservice.stripe.StripeClient;
import com.stripe.model.Charge;
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

import java.math.BigDecimal;

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

    @Autowired
    StripeClient stripeClient;

    @Autowired
    WalletHistoryService walletHistoryService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String signatureHeader) {
        try {
            //pdLogger.logException(PdLogger.EVENT.STRIPE_WEBHOOK, new Exception("WEBHOOK" + "Callback recieved for type " + payload));
            Event event = Webhook.constructEvent(
                    payload,
                    signatureHeader,
                    secretKey
            );
            pdLogger.logInfo("Webhook", "Callback Successfull for  " + event.getType());


            String message = "";
            PaymentIntent paymentIntent = null;
            String paymentIntentId = "";
            String sessionId = "";
            // Handle different types of events. We have configured stripe to listen to these events
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    paymentIntent = getPaymentIntentId(event);
                    paymentIntentId = paymentIntent.getId();
                    sessionId = stripeClient.getSessionId(paymentIntentId);
                    message = paymentService.completePaymentToBuyTrees(paymentIntentId, sessionId);
                    break;
                case "payment_intent.payment_failed":
                    paymentIntent = getPaymentIntentId(event);
                    paymentIntentId = paymentIntent.getId();
                    sessionId = stripeClient.getSessionId(paymentIntentId);
                    message = paymentService.failPaymentToBuyTrees(paymentIntentId, sessionId);
                    break;
                case "charge.refunded":
                    Charge charge = (Charge) event.getData().getObject();
                    paymentIntentId = charge.getPaymentIntent();
                    Long valueOfOneTreeInCents = 11L;
                    Long amountToRefundInCents = charge.getAmountRefunded();
                    Long treesToRefund = amountToRefundInCents / valueOfOneTreeInCents;
                    message = paymentService.completeRefundTrees(new BigDecimal(amountToRefundInCents), new BigDecimal(treesToRefund), paymentIntentId);
                    break;
                default:
                    break;
            }
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.STRIPE_WEBHOOK, e);
            return new ResponseEntity<>("Webhook processing failed with following exception " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private PaymentIntent getPaymentIntentId(Event event) {
        String paymentIntentId = null;
        if ("payment_intent.succeeded".equals(event.getType()) ||
                "payment_intent.payment_failed".equals(event.getType()) ||
                "charge.refunded".equals(event.getType())) {

            return (PaymentIntent) event.getData().getObject();
        }
        return null;
    }
}
