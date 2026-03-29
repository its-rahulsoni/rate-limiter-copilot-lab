package com.example.ratelimiter.core.registry;

import com.example.ratelimiter.core.strategy.RateLimitingStrategy;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiterRegistry {
    // ...Guava Cache or ConcurrentHashMap for per-key strategies
    // ...methods to get/create per-key RateLimitingStrategy
}
