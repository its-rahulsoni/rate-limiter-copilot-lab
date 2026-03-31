package com.example.ratelimiter.core.strategy;

import com.example.ratelimiter.core.api.RateLimiter;
import com.example.ratelimiter.core.config.RateLimiterConfig;
import com.example.ratelimiter.core.model.RateLimitResult;

import java.time.Clock;

public class FixedWindowRateLimiter implements RateLimiter {

    public FixedWindowRateLimiter(RateLimiterConfig config, Clock clock) {
            if (config.getCapacity() <= 0) {
                throw new IllegalArgumentException("Fixed window capacity must be > 0");
            }
            if (config.getRefillRate() <= 0) {
                throw new IllegalArgumentException("Fixed window refillRate must be > 0");
            }
    }

    @Override
    public boolean allow() {
        return false;
    }

    @Override
    public RateLimitResult check() {
        return null;
    }
}
