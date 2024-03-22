package com.pding.paymentservice.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.PriceListParams;
import com.stripe.param.ProductListParams;
import com.stripe.param.checkout.SessionCreateParams;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.stripe.param.checkout.SessionCreateParams.Mode.PAYMENT;

@Component
public class StripeClient {
    @Value("${stripe.secret.key}")
    private String secretKey;

    @Autowired
    StripeClient() {
        Stripe.apiKey = secretKey;
    }

    public StripeClientResponse createStripeSession(String productId, String successUrl, String cancelUrl) throws Exception {
        Stripe.apiKey = secretKey;

        Product product = getProduct(productId);
        String priceId = getPriceIdForProduct(product.getId());

        SessionCreateParams.Builder builder = new SessionCreateParams.Builder();
        builder.setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addLineItem(new SessionCreateParams.LineItem.Builder()
                        .setQuantity(1L)
                        .setPrice(priceId)
                        .build())
                .setMode(PAYMENT);

        Map<String, Object> params = builder.build().toMap();
        Session session = Session.create(params);
        session.getPaymentIntent();
        return new StripeClientResponse(product, session);
    }

    // Retrieve a Stripe product by its ID
    private Product getProduct(String productId) throws StripeException {
        Stripe.apiKey = secretKey;
        return Product.retrieve(productId);
    }

    private String getPriceIdForProduct(String productId) throws StripeException {
        Stripe.apiKey = secretKey;

        PriceListParams params = PriceListParams.builder()
                .setProduct(productId)
                .build();

        List<Price> prices = Price.list(params).getData();
        
        if (!prices.isEmpty()) {
            return prices.get(0).getId();
        } else {
            throw new RuntimeException("No prices found for the product ID: " + productId);
        }
    }

    public boolean isPaymentIntentIDPresentInStripe(String paymentIntentId) throws Exception {
        Stripe.apiKey = secretKey;
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return paymentIntent.getStatus().equals("succeeded");
    }

    public String getSessionId(String paymentIntentId) throws Exception {
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        // Build the request
        Request request = new Request.Builder()
                .url("https://api.stripe.com/v1/checkout/sessions?payment_intent=" + paymentIntentId)
                .method("GET", null) // No need for a request body in a GET request
                .addHeader("Authorization", "Bearer " + secretKey)
                .build();

        // Execute the request
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        JSONObject jsonObject = new JSONObject(responseBody);
        JSONArray dataArray = jsonObject.getJSONArray("data");
        JSONObject dataObject = dataArray.getJSONObject(0);
        String sessionId = dataObject.getString("id");
        return sessionId;
    }

    public Session getSessionDetails(String sessionId) throws Exception {
        Stripe.apiKey = secretKey;
        return Session.retrieve(sessionId);
    }

    public Boolean isSessionCompleteOrExpired(Session session) {
        return (session.getStatus().equals("expired") || session.getStatus().equals("complete"));
    }

    public Boolean isPaymentDone(Session session) {
        return (session.getPaymentStatus().equals("paid"));
    }

}