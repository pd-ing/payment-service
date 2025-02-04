package com.pding.paymentservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pding.paymentservice.network.PaypalNetworkService;
import com.pding.paymentservice.payload.request.PaymentRequest;
import com.pding.paymentservice.payload.response.PaypalOrderResponse;
import com.pding.paymentservice.payload.response.paypal.PayPalCaptureOrder;
import com.pding.paymentservice.payload.response.paypal.PurchaseUnit;
import com.pding.paymentservice.paymentclients.stripe.StripeClient;
import com.pding.paymentservice.security.AuthHelper;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PaypalService {

    @Autowired
    StripeClient stripeClient;

    @Autowired
    AuthHelper authHelper;

    @Autowired
    PaypalNetworkService paypalNetworkService;

    @Autowired
    PaymentService paymentService;

    public PaypalOrderResponse createOrder(String priceId) throws StripeException, JsonProcessingException {
        Price price = stripeClient.getPrice(priceId);
        BigDecimal amount = price.getUnitAmountDecimal().divide(BigDecimal.valueOf(100));
        String currency = price.getCurrency();
        String nickname = price.getNickname();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.readValue(nickname, Map.class);


        String userId = authHelper.getUserId();


        PaymentRequest.Amount requestedAmount = new PaymentRequest.Amount();
        requestedAmount.setCurrencyCode(currency);
        requestedAmount.setValue(amount);

        PaymentRequest.PurchaseUnit purchaseUnit = new PaymentRequest.PurchaseUnit();
        purchaseUnit.setAmount(requestedAmount);
        purchaseUnit.setCustomId(userId + "_" + map.get("trees"));

        List<PaymentRequest.PurchaseUnit> purchaseUnits = new ArrayList<>();
        purchaseUnits.add(purchaseUnit);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setIntent("CAPTURE");
        paymentRequest.setPurchaseUnits(purchaseUnits);

        return paypalNetworkService.createOrder(paymentRequest);
    }

    public PayPalCaptureOrder captureOrder(String orderId) {
//        CapturedPayment
        PayPalCaptureOrder captureOrderResponse = paypalNetworkService.captureOrder(orderId);
        if ("COMPLETED".equalsIgnoreCase(captureOrderResponse.getStatus())) {
            PurchaseUnit.Payments.Capture capture = captureOrderResponse.getPurchaseUnits().get(0).getPayments().getCaptures().get(0);
            com.pding.paymentservice.payload.response.paypal.Amount amount = capture.getAmount();
            String customId = capture.getCustomId();
            String transactionId = capture.getId();
            String[] userId_tree = customId.split("_");
            String userId = userId_tree[0];
            BigDecimal treeAmount = new BigDecimal(userId_tree[1]);

            if (paymentService.checkIfTxnIdExists(transactionId)) {
                log.info("paypal checkout, transaction is existed in DB");
                return captureOrderResponse;
            }

            paymentService.completePaymentToBuyTreesPaypal(userId, treeAmount, LocalDateTime.now(), transactionId,
                new BigDecimal(amount.getValue()), amount.getCurrencyCode(), "PAYPAL", captureOrderResponse.getStatus(),
                "Added " + treeAmount + " trees successfully for user via paypal",
                null);
        }

        return captureOrderResponse;
    }

    public void refund(String transactionId) {
        paymentService.completeRefundTreesByTransactionId(transactionId);
    }
}
