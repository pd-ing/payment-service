package com.pding.paymentservice.network;

import com.pding.paymentservice.payload.net.VideoPackageDetailsResponseNet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Service for making network calls to the content service
 */
@Component
@Slf4j
public class ContentNetworkService {

    private final WebClient webClient;

    @Value("${service.content.host}")
    private String contentServiceHost;

    @Autowired
    public ContentNetworkService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Get package details from content service
     * @param buyerId The buyer ID
     * @param packageId The package ID
     * @return Package details with personalized pricing for the buyer
     */
    public Mono<VideoPackageDetailsResponseNet> getPackageDetails(String packageId) {

        return webClient.get()
                .uri(contentServiceHost + "/api/internal/sale-package-details?packageId=" + packageId)
                .retrieve()
                .bodyToMono(VideoPackageDetailsResponseNet.class)
                .onErrorResume(e -> {
                    log.error("Error getting package details: {}", e.getMessage(), e);
                    return Mono.empty();
                });
    }
}
