package com.example.ratelimiter.core.strategy;

import com.example.ratelimiter.core.model.RateLimitResult;
import com.example.ratelimiter.core.config.RateLimiterConfig;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe, lazy-refill Token Bucket RateLimiter implementation.
 */
public class TokenBucketStrategy implements RateLimitingStrategy {
    private final int capacity;
    private final double refillRate; // tokens per second
    private double tokens;
    private Instant lastRefillTime;
    private final ReentrantLock lock = new ReentrantLock();

    public TokenBucketStrategy(RateLimiterConfig config, Clock clock) {
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
    }

    @Override
    public RateLimitResult check(Clock clock) {
        lock.lock();
        try {
            Instant now = clock.instant();
            double elapsedSeconds = (now.toEpochMilli() - lastRefillTime.toEpochMilli()) / 1000.0;
            if (elapsedSeconds > 0) {
                double tokensToAdd = elapsedSeconds * refillRate;
                tokens = Math.min(capacity, tokens + tokensToAdd);
                lastRefillTime = lastRefillTime.plusMillis((long)(elapsedSeconds * 1000));
            }
            boolean allowed = tokens >= 1.0;
            double retryAfter = 0;
            if (allowed) {
                tokens -= 1.0;
            } else {
                double tokensNeeded = 1.0 - tokens;
                retryAfter = tokensNeeded / refillRate;
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
