package com.pding.paymentservice.controllers;

import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.service.PaymentService;
import com.pding.paymentservice.service.WalletHistoryService;
import com.pding.paymentservice.paymentclients.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Price;
import com.stripe.model.Refund;
import lombok.extern.log4j.Log4j2;
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
import java.math.RoundingMode;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")

@Log4j2
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

    Double valueOfOneTreeInCents = 8.8;

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
//            pdLogger.logInfo("Webhook", "Callback Successfull for  " + event.getType());


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
                    log.info("handling Stripe payment_intent.succeeded event for paymentIntentId: {}", paymentIntentId, "sessionId: " + sessionId);
                    message = paymentService.completePaymentToBuyTrees(paymentIntentId, sessionId);
                    break;
                case "payment_intent.payment_failed":
                    paymentIntent = getPaymentIntentId(event);
                    paymentIntentId = paymentIntent.getId();
                    sessionId = stripeClient.getSessionId(paymentIntentId);
                    log.info("handling Stripe payment_intent.payment_failed event for paymentIntentId: {}", paymentIntentId, "sessionId: " + sessionId);
                    message = paymentService.failPaymentToBuyTrees(paymentIntentId, sessionId);
                    break;
                case "charge.refunded":
                    Charge charge = (Charge) event.getData().getObject();
                    paymentIntentId = charge.getPaymentIntent();
                    String chargeId = charge.getId();
                    Long amountToRefund = charge.getAmountRefunded();
                    String currency = charge.getCurrency();
                    Long treesToRefund;
                    if (currency.equalsIgnoreCase("krw")) {
                        treesToRefund = (new BigDecimal(amountToRefund)
                            .divide(getPriceOfTreeInWon(), 2, RoundingMode.UP)
                            .divide(BigDecimal.valueOf(1.1 * 0.8), 0, RoundingMode.UP))
                            .longValue();
                    } else {
                        treesToRefund = (long) Math.ceil(amountToRefund / valueOfOneTreeInCents);
                    }

                    log.info("handling Stripe charge.refunded event for paymentIntentId: {}, amountToRefund: {}, treesToRefund: {}", paymentIntentId, amountToRefund, treesToRefund);
                    // comment because this event send all refund amount, not this refund amount
                    //                    message = paymentService.completeRefundTrees(new BigDecimal(amountToRefund), new BigDecimal(treesToRefund), paymentIntentId, chargeId);
                    break;
                case "charge.refund.updated":
                    Refund refund = (Refund) event.getData().getObject();
                    currency = refund.getCurrency();
                    amountToRefund = refund.getAmount();
                    String refundId = refund.getId();
                    paymentIntentId = refund.getPaymentIntent();
                    long treesToAdd;

                    if (currency.equalsIgnoreCase("krw")) {
                        treesToAdd = (new BigDecimal(amountToRefund)
                            .divide(getPriceOfTreeInWon(), 2, RoundingMode.UP)
                            .divide(BigDecimal.valueOf(1.1 * 0.8), 0, RoundingMode.UP))
                            .longValue();
                    } else {
                        treesToAdd = (long) Math.ceil(amountToRefund / valueOfOneTreeInCents);
                    }

                    String transactionId = treesToAdd + "_trees_refunded_for_" + refund.getPaymentIntent();
                    if (refund.getStatus().equals("canceled")) {
                        log.info("handling Stripe charge.refund.updated, status CANCEL for transactionId: {}, treesToAdd: {}", transactionId, treesToAdd);
                        message = paymentService.cancelRefundTrees(new BigDecimal(treesToAdd), transactionId);
                    }
                    break;
                case "refund.created":
                    refund = (Refund) event.getData().getObject();
                    currency = refund.getCurrency();
                    amountToRefund = refund.getAmount();
                    refundId = refund.getId();
                    paymentIntentId = refund.getPaymentIntent();

                    if (currency.equalsIgnoreCase("krw")) {
                        treesToAdd = (new BigDecimal(amountToRefund)
                            .divide(getPriceOfTreeInWon(), 2, RoundingMode.UP)
                            .divide(BigDecimal.valueOf(1.1 * 0.8), 0, RoundingMode.UP))
                            .longValue();
                    } else {
                        treesToAdd = (long) Math.ceil(amountToRefund / valueOfOneTreeInCents);
                    }

                    message = paymentService.completeRefundTrees(new BigDecimal(amountToRefund), new BigDecimal(treesToAdd), paymentIntentId, refundId);
                    break;
                default:
                    break;
            }
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (Exception e) {
            pdLogger.logException(PdLogger.EVENT.STRIPE_WEBHOOK, e);
            return new ResponseEntity<>("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
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

    @Value("${stripe.product.OneHundredTreesKwonProdId}")
    String productOneHundredTreesKwonProdId;

    private BigDecimal getPriceOfTreeInWon() throws StripeException {
        Price aPriceOfProduct = stripeClient.getListActivePrice(productOneHundredTreesKwonProdId, 1l).get(0);
        BigDecimal amount = aPriceOfProduct.getUnitAmountDecimal();
        return amount.divide(new BigDecimal(110), 2, RoundingMode.UP);
    }
}
