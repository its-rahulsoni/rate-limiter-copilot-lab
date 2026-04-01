package com.example.ratelimiter.core.algorithm;

import com.example.ratelimiter.core.api.RateLimiter;
import com.example.ratelimiter.core.model.RateLimitResult;
import com.example.ratelimiter.core.config.RateLimiterConfig;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Leaky Bucket Rate Limiter implementation.
 * Thread-safe, production-ready, follows LeakyBucketAlgorithm.md specification.
 */
public class LeakyBucketRateLimiter implements RateLimiter {
    private final int capacity;
    private final double leakRate; // requests per second
    private double currentWater;
    private Instant lastLeakTime;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();

    public LeakyBucketRateLimiter(RateLimiterConfig config, Clock clock) {
        this.capacity = config.getCapacity();
        this.leakRate = config.getRefillRate(); // interpreted as leakRate (requests/sec)
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        if (leakRate <= 0) throw new IllegalArgumentException("leakRate must be > 0");
        this.currentWater = 0.0;
        this.clock = clock;
        this.lastLeakTime = clock.instant();
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
            double elapsedSeconds = (now.toEpochMilli() - lastLeakTime.toEpochMilli()) / 1000.0;
            // Step 1: Calculate leakage
            double leaked = elapsedSeconds * leakRate;
            currentWater = Math.max(0.0, currentWater - leaked);
            // Step 2: Update last leak time
            lastLeakTime = now;
            boolean allowed = false;
            double retryAfter = 0.0;
            // Step 3: Apply rate limiting
            if (currentWater + 1.0 <= capacity) {
                currentWater += 1.0;
                allowed = true;
            } else {
                double excessWater = currentWater - capacity + 1.0;
                retryAfter = excessWater / leakRate;
            }
            int remainingCapacity = (int) Math.floor(Math.max(0.0, capacity - currentWater));
            return new RateLimitResult(
                allowed,
                retryAfter,
                remainingCapacity,
                capacity
            );
        } finally {
            lock.unlock();
        }
    }
}
