package com.example.ratelimiter.core.strategy;

import com.example.ratelimiter.core.model.RateLimitResult;
import java.time.Clock;

public class FixedWindowStrategy implements RateLimitingStrategy {
    @Override
    public RateLimitResult check(Clock clock) {
        // ...fixed window logic using clock.instant().toEpochMilli()
        return null; // placeholder
    }
}
