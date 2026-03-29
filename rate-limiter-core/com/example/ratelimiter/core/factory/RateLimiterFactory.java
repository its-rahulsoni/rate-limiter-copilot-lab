package com.example.ratelimiter.core.factory;

import com.example.ratelimiter.core.api.RateLimiter;
import com.example.ratelimiter.core.strategy.RateLimitingStrategy;
import com.example.ratelimiter.core.config.RateLimiterConfig;
import java.time.Clock;

public class RateLimiterFactory {
    private final RateLimitingStrategy strategy;
    private final RateLimiterConfig config;
    private final Clock clock;

    public RateLimiterFactory(RateLimitingStrategy strategy, RateLimiterConfig config, Clock clock) {
        this.strategy = strategy;
        this.config = config;
        this.clock = clock;
    }

    public RateLimiter createRateLimiter() {
        // TODO: Return a new RateLimiter instance using the strategy, config, and clock
        return null;
    }
}
