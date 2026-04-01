package com.example.ratelimiter.core.algorithm;

import com.example.ratelimiter.core.api.RateLimiter;
import com.example.ratelimiter.core.model.RateLimitResult;
import com.example.ratelimiter.core.config.RateLimiterConfig;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Per-key, stateful Token Bucket RateLimiter implementation.
 */
public class TokenBucketRateLimiter implements RateLimiter {
    private final int capacity;
    private final double refillRate; // tokens per second
    private double tokens;
    private Instant lastRefillTime;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();

    public TokenBucketRateLimiter(RateLimiterConfig config, Clock clock) {
        if (config.getCapacity() <= 0) {
            throw new IllegalArgumentException("Token bucket capacity must be > 0");
        }
        if (config.getRefillRate() <= 0) {
            throw new IllegalArgumentException("Token bucket refillRate must be > 0");
        }
        this.capacity = config.getCapacity();
        this.refillRate = config.getRefillRate();
        this.tokens = capacity;
        this.lastRefillTime = clock.instant();
        this.clock = clock;
    }

    @Override
    public boolean allow() {
        return check().isAllowed();
    }

    @Override
    public RateLimitResult check() {
        lock.lock();
        try {
            Instant now = clock.instant();

            double elapsedTime =
                    (now.toEpochMilli() - lastRefillTime.toEpochMilli()) / 1000.0;

            if (elapsedTime < 0) {
                elapsedTime = 0;
            }

            if (elapsedTime > 0) {
                double tokensToAdd = elapsedTime * refillRate;
                tokens = Math.min(capacity, tokens + tokensToAdd);

                // FIX: always set to now
                lastRefillTime = now;
            }

            boolean allowed = tokens >= 1.0;
            double retryAfter = 0;

            if (allowed) {
                tokens -= 1.0;
            } else {
                double tokensNeeded = 1.0 - tokens;

                if (refillRate > 0) {
                    retryAfter = tokensNeeded / refillRate;
                } else {
                    retryAfter = Double.MAX_VALUE;
                }
            }

            return new RateLimitResult(
                    allowed,
                    retryAfter,
                    (int) Math.floor(tokens),
                    capacity
            );

        } finally {
            lock.unlock();
        }
    }
}
