package com.example.ratelimiter.core.algorithm;

import com.example.ratelimiter.core.api.RateLimiter;
import com.example.ratelimiter.core.model.RateLimitResult;
import com.example.ratelimiter.core.config.RateLimiterConfig;
import java.time.Clock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Fixed Window Rate Limiter implementation.
 * Thread-safe, production-ready, follows ALGORITHMS.md specification.
 */
public class FixedWindowRateLimiter implements RateLimiter {
    private final int limit; // limit is the maximum number of requests per window (from config.getCapacity()).
    private final long windowSize; // windowSize is the window duration in milliseconds (from config.getRefillRate(), which is mapped to window size).
    private final Clock clock;
    private long currentWindowId; // currentWindowId tracks the current time window (e.g., 5-min window, 1-sec window, etc.).
    private int requestCount; // requestCount tracks the number of requests in the current window.
    private final ReentrantLock lock = new ReentrantLock();

    public FixedWindowRateLimiter(RateLimiterConfig config, Clock clock) {
        this.limit = config.getCapacity(); // 'limit' in config
        this.windowSize = (long) config.getRefillRate(); // 'windowSize' in ms, mapped from refillRate
        this.clock = clock;
        long now = clock.millis();
        this.currentWindowId = now / windowSize;
        this.requestCount = 0;
    }

    @Override
    public boolean allow() {
        return check().isAllowed();
    }

    @Override
    public RateLimitResult check() {
        lock.lock();
        try {
            long now = clock.millis();
            long windowId = now / windowSize;
            if (windowId != currentWindowId) {
                currentWindowId = windowId;
                requestCount = 0;
            }
            boolean allowed = requestCount < limit;
            double retryAfter = 0;
            if (allowed) {
                requestCount++;
            } else {
                long windowEnd = (currentWindowId + 1) * windowSize;
                retryAfter = (windowEnd - now) / 1000.0;
            }
            int remaining = Math.max(0, limit - requestCount);
            return new RateLimitResult(
                allowed,
                retryAfter,
                remaining,
                limit
            );
        } finally {
            lock.unlock();
        }
    }
}
