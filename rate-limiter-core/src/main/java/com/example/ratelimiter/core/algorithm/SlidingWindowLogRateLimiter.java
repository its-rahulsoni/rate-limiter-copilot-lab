package com.example.ratelimiter.core.algorithm;

import com.example.ratelimiter.core.api.RateLimiter;
import com.example.ratelimiter.core.model.RateLimitResult;
import com.example.ratelimiter.core.config.RateLimiterConfig;
import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Sliding Window Log Rate Limiter implementation.
 * Thread-safe, production-ready, follows SlidingWindowLogAlgorithm.md specification.
 */
public class SlidingWindowLogRateLimiter implements RateLimiter {
    private final long windowSizeInMillis;
    private final int maxRequests;
    private final Clock clock;
    private final Deque<Long> timestamps;

    public SlidingWindowLogRateLimiter(RateLimiterConfig config, Clock clock) {
        this.windowSizeInMillis = (long) config.getRefillRate(); // window size in ms
        this.maxRequests = config.getCapacity();
        this.clock = clock;
        this.timestamps = new ArrayDeque<>();
        if (windowSizeInMillis <= 0) throw new IllegalArgumentException("windowSizeInMillis must be > 0");
        if (maxRequests <= 0) throw new IllegalArgumentException("maxRequests must be > 0");
    }

    @Override
    public synchronized boolean allow() {
        return check().isAllowed();
    }

    @Override
    public synchronized RateLimitResult check() {
        long now = clock.millis();
        long windowStart = now - windowSizeInMillis;
        // Remove expired timestamps (<= windowStart)
        while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
            timestamps.pollFirst();
        }
        boolean allowed = timestamps.size() < maxRequests;
        double retryAfter = 0.0;
        if (allowed) {
            timestamps.addLast(now);
        } else {
            // Time until the oldest request expires
            Long oldest = timestamps.peekFirst();
            if (oldest == null) {
                retryAfter = 0.0;
            } else {
                retryAfter = (oldest + windowSizeInMillis - now) / 1000.0;
            }
        }
        int remaining = Math.max(0, maxRequests - timestamps.size());
        return new RateLimitResult(
            allowed,
            retryAfter,
            remaining,
            maxRequests
        );
    }
}
