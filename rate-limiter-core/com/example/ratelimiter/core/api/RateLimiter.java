package com.example.ratelimiter.core.api;

import com.example.ratelimiter.core.model.RateLimitResult;

public interface RateLimiter {
    boolean allow(String key);
    RateLimitResult check(String key);
}
