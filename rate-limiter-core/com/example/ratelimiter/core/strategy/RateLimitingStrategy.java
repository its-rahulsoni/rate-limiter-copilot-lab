package com.example.ratelimiter.core.strategy;

import com.example.ratelimiter.core.model.RateLimitResult;
import java.time.Clock;

public interface RateLimitingStrategy {
    RateLimitResult check(Clock clock);
}
