package com.pding.paymentservice.paymentclients.stripe;

import com.pding.paymentservice.repository.WalletRepository;
import com.pding.paymentservice.security.AuthHelper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Price;
import com.stripe.model.PriceCollection;
import com.stripe.model.Product;
import com.stripe.model.ProductCollection;
import com.stripe.model.Transfer;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PriceListParams;
import com.stripe.param.ProductListParams;
import com.stripe.param.TransferCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.stripe.param.checkout.SessionCreateParams.Mode.PAYMENT;

@Component
public class StripeClient {
    @Autowired
    WalletRepository walletRepository;

    @Autowired
    AuthHelper authHelper;

    public StripeClientResponse createStripeSession(String productId, String successUrl, String cancelUrl) throws Exception {
        Product product = getProduct(productId);
        String priceId = getPriceIdForProduct(product.getId());

        String userId = authHelper.getUserId();
        Optional<String> userIdOptional = walletRepository.findEmailById(userId);
        if (!userIdOptional.isPresent()) {
            throw new Exception("No emailId found for the userId " + userId);
        }

        SessionCreateParams.Builder builder = new SessionCreateParams.Builder();
        builder.setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
//                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setAutomaticTax(SessionCreateParams.AutomaticTax.builder()
                        .setEnabled(true)
                        .build())
                .addLineItem(new SessionCreateParams.LineItem.Builder()
                        .setQuantity(1L)
                        .setPrice(priceId)
                        .build())
                .setMode(PAYMENT)
                .setCustomerEmail(userIdOptional.get());
        Map<String, Object> params = builder.build().toMap();
        Session session = Session.create(params);
        session.getPaymentIntent();
        return new StripeClientResponse(product, session);
    }

    // Method to check the status of a payment based on the payment intent ID
    public String checkPaymentStatus(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            String status = paymentIntent.getStatus();

            // Return the status
            return status;
        } catch (Exception e) {
            return "No payment status found for " + paymentIntentId;
        }

    }

    // Retrieve a Stripe product by its ID
    private Product getProduct(String productId) throws StripeException {
        return Product.retrieve(productId);
    }

    public Map<String, String> createPaymentIntent(String priceId) throws StripeException {
        Price price = getPrice(priceId);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(price.getUnitAmount()) // Amount in cents
            .setCurrency(price.getCurrency())
            .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
            .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", paymentIntent.getClientSecret());
        return response;
    }


    public Price getPrice(String priceId) throws StripeException {
        return Price.retrieve(priceId);
    }

    private String getPriceIdForProduct(String productId) throws StripeException {
        PriceListParams params = PriceListParams.builder()
                .setProduct(productId)
                .build();

        List<Price> prices = Price.list(params).getData();


        if (prices.isEmpty()) {
            throw new RuntimeException("No prices found for the product ID: " + productId);
        }

        for (Price price : prices) {
            // Check if the price is active
            if (price.getActive()) {
                return price.getId();
            }
        }

        throw new RuntimeException("No active price found for the product ID: " + productId);
    }

    public boolean isPaymentIntentIDPresentInStripe(String paymentIntentId) throws Exception {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return paymentIntent.getStatus().equals("succeeded");
    }

    public String getSessionId(String paymentIntentId) throws Exception {
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        // Build the request
        Request request = new Request.Builder()
                .url("https://api.stripe.com/v1/checkout/sessions?payment_intent=" + paymentIntentId)
                .method("GET", null) // No need for a request body in a GET request
                .addHeader("Authorization", "Bearer " + Stripe.apiKey)
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
        return Session.retrieve(sessionId);
    }

    public Transfer transferPayment(String stripeId, String recipientEmail, Long amountInCents, Map<String, String> metaData) throws Exception {
        TransferCreateParams createParams = TransferCreateParams.builder()
                .setAmount(amountInCents) // amount in cents
                .setCurrency("usd")
                .setDestination(stripeId)
                .setDescription("Commission for the referrer")
                .putAllMetadata(metaData)
                .build();

        return Transfer.create(createParams);
    }

    public Boolean isSessionCompleteOrExpired(Session session) {
        return (session.getStatus().equals("expired") || session.getStatus().equals("complete"));
    }

    public Boolean isPaymentDone(Session session) {
        return (session.getPaymentStatus().equals("paid"));
    }

    public List<Product> getListActiveProduct(Long limit) throws StripeException {
        ProductListParams params = ProductListParams.builder()
            .setLimit(limit)
            .setActive(true)
            .build();

        ProductCollection products = Product.list(params);
        return products.getData();
    }

    public List<Price> getListActivePrice(String productId, Long limit) throws StripeException {
        PriceListParams params = PriceListParams.builder()
            .setProduct(productId)
            .setActive(true)
            .setLimit(limit)
            .build();
        PriceCollection prices = Price.list(params);
        return prices.getData();
    }
}
