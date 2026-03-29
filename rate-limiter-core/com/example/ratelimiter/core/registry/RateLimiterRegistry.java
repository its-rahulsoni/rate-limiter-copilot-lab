package com.example.ratelimiter.core.registry;

import com.example.ratelimiter.core.api.RateLimiter;
import com.example.ratelimiter.core.factory.RateLimiterFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages per-key RateLimiter instances using in-memory cache and RateLimiterFactory for creation.
 */
public class RateLimiterRegistry {
    private final ConcurrentMap<String, RateLimiter> limiterMap = new ConcurrentHashMap<>();
    private final RateLimiterFactory factory;

    public RateLimiterRegistry(RateLimiterFactory factory) {
        this.factory = factory;
    }

    public RateLimiter getOrCreate(String key) {
        return limiterMap.computeIfAbsent(key, k -> factory.createRateLimiter());
    }

    // Optionally: add eviction, cleanup, etc.
}
