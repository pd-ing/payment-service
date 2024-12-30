package com.pding.paymentservice.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pding.paymentservice.payload.request.PaymentRequest;
import com.pding.paymentservice.payload.response.PaypalAuthenticationResponse;
import com.pding.paymentservice.payload.response.PaypalOrderResponse;
import com.pding.paymentservice.payload.response.paypal.PayPalCaptureOrder;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;

@Service
public class PaypalNetworkService {

    private final WebClient webClient;
    private final OkHttpClient client;

    @Value("${paypal.baseUrl}")
    private String paypalBaseUrl;

    @Value("${paypal.clientId}")
    private String clientId;

    @Value("${paypal.clientSecret}")
    private String clientSecret;


    @Autowired
    public PaypalNetworkService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        this.client = new OkHttpClient();
    }

    public String generateAccessToken() {
        String auth = Base64.getEncoder().encodeToString(
            (clientId + ":" + clientSecret).getBytes()
        );
        String uri = paypalBaseUrl + "/v1/oauth2/token";
        Mono<PaypalAuthenticationResponse> responseMono = webClient.post()
            .uri(uri)
            .header("Authorization", "Basic " + auth)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue("grant_type=client_credentials")
            .retrieve()
            .bodyToMono(PaypalAuthenticationResponse.class);

        return responseMono.block().getAccessToken();

    }

    public PaypalOrderResponse createOrder(PaymentRequest paymentRequest) {
        String accessToken = generateAccessToken();

        if (accessToken == null) {
            throw new IllegalStateException("Failed to generate access token.");
        }

        String uri = paypalBaseUrl + "/v2/checkout/orders";

        Mono<PaypalOrderResponse> responseMono = webClient.post()
            .uri(uri)
            .header("Authorization", "Bearer " + accessToken)
            .header("Content-Type", "application/json")
            .body(BodyInserters.fromValue(paymentRequest))
            .retrieve()
            .bodyToMono(PaypalOrderResponse.class);

        return responseMono.block();

    }

    public PayPalCaptureOrder captureOrder(String orderId) {
        String accessToken = generateAccessToken();

        if (accessToken == null) {
            throw new IllegalStateException("Failed to generate access token.");
        }

        String uri = paypalBaseUrl + "/v2/checkout/orders/" + orderId + "/capture";

        Mono<PayPalCaptureOrder> responseMono = webClient.post()
            .uri(uri)
            .header("Authorization", "Bearer " + accessToken)
            .header("Content-Type", "application/json")
            .retrieve()
            .bodyToMono(PayPalCaptureOrder.class);

        return responseMono.block();
    }
}
