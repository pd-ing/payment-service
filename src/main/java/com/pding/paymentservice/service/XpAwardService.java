package com.pding.paymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

@Service
@Slf4j
public class XpAwardService {

    private final WebClient webClient;
    
    @Value("${service.user.host}")
    private String userManagementHost;

    @Autowired
    public XpAwardService(WebClient.Builder webClientBuilder, 
                         @Value("${service.user.host}") String userManagementHost) {
        this.userManagementHost = userManagementHost;
        this.webClient = webClientBuilder
                .baseUrl(userManagementHost)
                .build();
    }
    
    public void awardXpForTreeUsage(String userId, BigDecimal treesUsed, String sourceId, String sourceType) {
        try {
            log.info("Attempting to award XP for user {}: {} XP to host: {}", userId, treesUsed, userManagementHost);
            
            XpAwardRequest request = XpAwardRequest.builder()
                    .userId(userId)
                    .xpAmount(treesUsed) // 1 Tree = 1 XP
                    .xpType("TREE_USAGE")
                    .sourceId(sourceId)
                    .sourceType(sourceType)
                    .serviceSource("payment-service")
                    .description("Tree usage: " + treesUsed + " trees")
                    .build();

            webClient.post()
                    .uri("/api/user/internal/award-xp")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(response -> log.info("XP awarded successfully for user {}: {} XP", userId, treesUsed))
                    .doOnError(error -> log.error("Failed to award XP for user {}: {} - Error: {}", userId, treesUsed, error.getMessage()))
                    .subscribe();
                    
        } catch (Exception e) {
            log.error("Error awarding XP for user {}: {} - Exception: {}", userId, treesUsed, e.getMessage(), e);
        }
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class XpAwardRequest {
        private String userId;
        private BigDecimal xpAmount;
        private String xpType;
        private String sourceId;
        private String sourceType;
        private String serviceSource;
        private String description;
    }
}
