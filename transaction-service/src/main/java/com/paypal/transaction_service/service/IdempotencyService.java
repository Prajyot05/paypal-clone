package com.paypal.transaction_service.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private static final String IDEMPOTENCY_PREFIX = "idem_txn_";

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Attempts to acquire an idempotency lock for the given key.
     * @param key The idempotency key
     * @return true if this is the first time we see this key (lock acquired), false if it's a duplicate
     */
    public boolean checkAndSetIdempotency(String key) {
        if (key == null || key.isBlank()) return true; // Without key, we allow it (or you can reject)
        
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(IDEMPOTENCY_PREFIX + key, "PROCESSING", 24, TimeUnit.HOURS);
                
        return Boolean.TRUE.equals(acquired);
    }
}
