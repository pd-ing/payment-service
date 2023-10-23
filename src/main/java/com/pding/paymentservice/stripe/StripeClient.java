package com.pding.paymentservice.stripe;

import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StripeClient {
    @Value("${stripe.secret.key}")
    private String secretKey;

    @Autowired
    StripeClient() {
        Stripe.apiKey = secretKey;
    }

    public Customer createCustomer(String token, String email) throws Exception {
        Map<String, Object> customerParams = new HashMap<String, Object>();
        customerParams.put("email", email);
        customerParams.put("source", token);
        return Customer.create(customerParams);
    }

    private Customer getCustomer(String id) throws Exception {
        return Customer.retrieve(id);
    }

    public Charge chargeNewCard(String token, double amount) throws Exception {
        Stripe.apiKey = secretKey;
        Map<String, Object> chargeParams = new HashMap<String, Object>();
        chargeParams.put("amount", (int) (amount * 100));
        chargeParams.put("currency", "USD");
        chargeParams.put("source", token);
        Charge charge = Charge.create(chargeParams);
        return charge;
    }

    public Charge chargeCustomerCard(String customerId, int amount) throws Exception {
        String sourceCard = getCustomer(customerId).getDefaultSource();
        Map<String, Object> chargeParams = new HashMap<String, Object>();
        chargeParams.put("amount", amount);
        chargeParams.put("currency", "USD");
        chargeParams.put("customer", customerId);
        chargeParams.put("source", sourceCard);
        Charge charge = Charge.create(chargeParams);
        return charge;
    }

    public boolean isTransactionIdPresentInStripe(String paymentIntentId) throws Exception {
        Stripe.apiKey = secretKey;
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return paymentIntent.getStatus().equals("succeeded");
    }
}