package com.pding.paymentservice.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class PdingHealthIndicator implements HealthIndicator {
//    @Autowired
//    private StringRedisTemplate redisTemplate;

    @Override
    public Health health() {
        boolean isHealthy = checkServiceHealth();
        if (isHealthy) {
            return Health.up().withDetail("message", "Service is healthy").build();
        }
        return Health.down().withDetail("message", "Service is down").build();
    }

    private boolean checkServiceHealth() {
        return true;
    }
}
