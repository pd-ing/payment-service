package com.pding.paymentservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import java.math.BigDecimal;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("api/payment/paypal")
public class PaypalController {

    @Autowired
    PaypalService paypalService;

    @PostMapping("/webhook")
    public void handlePaypalWebhook(@RequestBody String body) throws JsonProcessingException {
        ObjectNode json = new ObjectMapper().readValue(body, ObjectNode.class);
        String eventType = json.get("event_type").asText();

        if(eventType.equalsIgnoreCase("PAYMENT.CAPTURE.REFUNDED") || eventType.equalsIgnoreCase("PAYMENT.CAPTURE.REVERSED")) {
            ObjectNode resource = (ObjectNode) json.get("resource");
            ArrayNode links = (ArrayNode) resource.get("links");
            ObjectNode amount  = (ObjectNode) resource.get("amount");
            BigDecimal refundAmount = new BigDecimal(amount.get("value").asText());
            String refundId = resource.get("id").asText();

            //get rel up link (parrent transaction of refund)
            String upLink = null;
            for(int i = 0; i < links.size(); i++) {
                ObjectNode link = (ObjectNode) links.get(i);
                if(link.get("rel").asText().equalsIgnoreCase("up")) {
                    upLink = link.get("href").asText();
                    break;
                }
            }

            if(upLink != null) {
                //get transaction id from up link
                String transactionId = upLink.substring(upLink.lastIndexOf('/') + 1);
                paypalService.calculateTreesAndRollback(transactionId, refundAmount, refundId);
            }
        }
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
