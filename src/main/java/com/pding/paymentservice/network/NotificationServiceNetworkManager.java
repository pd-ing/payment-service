package com.pding.paymentservice.network;


import com.pding.paymentservice.PdLogger;
import com.pding.paymentservice.payload.request.fcm.SendGenericNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;


@Component
@RequiredArgsConstructor
public class NotificationServiceNetworkManager {
    private final WebClient webClient;

    @Value("${service.backend.host}")
    private String notificationService;

    @Autowired
    private PdLogger pdLogger;

    @Autowired
    public NotificationServiceNetworkManager(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Boolean sendGenericNotification(String userId, Map<String, String> data) {
        try {
            return _sendGenericNotification(userId, data).blockFirst();
        } catch (Exception ex) {
            pdLogger.logException(ex);
            return false;
        }
    }


    private Flux<Boolean> _sendGenericNotification(String userId, Map<String, String> data) {

        String url = notificationService + "/payment/tokens/sendGenericNotification";


        // Creating the request payload
        SendGenericNotificationRequest notificationRequest = SendGenericNotificationRequest.builder()
                .userId(userId)
                .data(data)
                .build();

        return webClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .body(BodyInserters.fromValue(notificationRequest)) // Sending JSON payload
                .exchangeToFlux(clientResponse -> {
                    if (clientResponse.statusCode().is2xxSuccessful()) {
                        return Flux.just(true);
                    } else {
                        return clientResponse.bodyToMono(String.class)
                                .doOnNext(body -> System.err.println("Non-2xx response: " + clientResponse.statusCode() + ", Body: " + body))
                                .flatMapMany(body -> Flux.just(false));
                    }
                })
                .onErrorResume(ex -> {
                    String errorMessage = ex.getMessage();
                    pdLogger.logException(new Exception(errorMessage));
                    System.err.println("Error occurred: " + errorMessage);
                    ex.printStackTrace();
                    return Flux.just(false);
                });
    }


}
