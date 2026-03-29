package com.example.ratelimiter.core.api;

import com.example.ratelimiter.core.model.RateLimitResult;

/**
 * Represents a per-key rate limiter instance (stateful).
 */
public interface RateLimiter {

    // Simple yes/no decision ....
    boolean allow();

    /**
     * allowed = true/false
     * retryAfter = seconds to wait
     * remainingTokens = how many left
     * limit = max capacity
     */
    RateLimitResult check();
}
