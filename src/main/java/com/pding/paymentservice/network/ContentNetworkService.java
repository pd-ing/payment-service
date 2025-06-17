package com.pding.paymentservice.network;

import com.pding.paymentservice.payload.net.PhotoPostResponseNet;
import com.pding.paymentservice.payload.net.VideoPackageDetailsResponseNet;
import com.pding.paymentservice.payload.response.PackageSalesStatsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Set;

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
     * @param packageId The package ID
     * @param selectedVideoIds Optional set of selected video IDs
     * @return Package details with personalized pricing for the buyer
     */
    public Mono<VideoPackageDetailsResponseNet> getPackageDetails(String packageId, Set<String> selectedVideoIds) {

        String uri = contentServiceHost + "/api/internal/sale-package-details?packageId=" + packageId;
        if (selectedVideoIds != null && !selectedVideoIds.isEmpty()) {
            uri += "&selectedVideoIds=" + String.join(",", selectedVideoIds);
        }
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(VideoPackageDetailsResponseNet.class)
                .onErrorResume(e -> {
                    log.error("Error getting package details: {}", e.getMessage(), e);
                    return Mono.empty();
                });
    }

    public boolean saveVideoPackageSalesStats(String packageId, int quantitySold, BigDecimal totalTreesEarned) {
        try {
            webClient.post()
                    .uri(contentServiceHost + "/api/internal/save-package-sales-stats")
                    .bodyValue(new PackageSalesStatsResponse(packageId, quantitySold, totalTreesEarned))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            return true;
        } catch (Exception e) {
            log.error("Error saving video package sales stats: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get photo post details from content service
     * @param postId The photo post ID
     * @return Photo post details
     */
    public Mono<PhotoPostResponseNet> getPhotoPostDetails(String postId) {
        String uri = contentServiceHost + "/api/internal/photo-post?postId=" + postId;
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(PhotoPostResponseNet.class)
                .onErrorResume(e -> {
                    log.error("Error getting photo post details: {}", e.getMessage(), e);
                    return Mono.empty();
                });
    }
}
