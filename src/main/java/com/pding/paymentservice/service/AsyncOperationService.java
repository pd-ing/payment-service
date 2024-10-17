package com.pding.paymentservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncOperationService {


    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Async
    public CompletableFuture<Boolean> removeCachePattern(String pattern) {
        //delete async redis cache
        Set<String> keySet = redisTemplate.keys(pattern);
        redisTemplate.delete(keySet);
        return CompletableFuture.completedFuture(true);
    }
}
