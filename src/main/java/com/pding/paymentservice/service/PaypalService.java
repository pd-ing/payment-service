package com.pding.paymentservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pding.paymentservice.network.PaypalNetworkService;
import com.pding.paymentservice.payload.request.PaymentRequest;
import com.pding.paymentservice.payload.response.PaypalOrderResponse;
import com.pding.paymentservice.paymentclients.stripe.StripeClient;
import com.pding.paymentservice.security.AuthHelper;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PaypalService {

    @Autowired
    StripeClient stripeClient;

    @Autowired
    AuthHelper authHelper;

    @Autowired
    PaypalNetworkService paypalNetworkService;

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
        purchaseUnit.setCustomId(userId+ "_" + map.get("trees"));

        List<PaymentRequest.PurchaseUnit> purchaseUnits = new ArrayList<>();
        purchaseUnits.add(purchaseUnit);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setIntent("CAPTURE");
        paymentRequest.setPurchaseUnits(purchaseUnits);

        return paypalNetworkService.createOrder(paymentRequest);
    }
}
