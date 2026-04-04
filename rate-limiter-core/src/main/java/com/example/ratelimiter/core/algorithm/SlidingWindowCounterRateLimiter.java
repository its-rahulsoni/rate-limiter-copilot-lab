package com.example.ratelimiter.core.algorithm;

import com.example.ratelimiter.core.api.RateLimiter;
import com.example.ratelimiter.core.model.RateLimitResult;
import com.example.ratelimiter.core.config.RateLimiterConfig;
import java.time.Clock;

/**
 * Sliding Window Counter Rate Limiter implementation.
 * Thread-safe, production-ready, follows SlidingWindowCounterImpl.md specification.
 */
public class SlidingWindowCounterRateLimiter implements RateLimiter {
    private final long windowSizeInMillis;
    private final int maxRequests;
    private final Clock clock;
    private int currentBucketCount;
    private int previousBucketCount;
    private long currentBucketStartTime;

    public SlidingWindowCounterRateLimiter(RateLimiterConfig config, Clock clock) {
        this.windowSizeInMillis = (long) config.getRefillRate();
        this.maxRequests = config.getCapacity();
        this.clock = clock;
        if (windowSizeInMillis <= 0) throw new IllegalArgumentException("windowSizeInMillis must be > 0");
        if (maxRequests <= 0) throw new IllegalArgumentException("maxRequests must be > 0");
        this.currentBucketStartTime = clock.millis();
        this.currentBucketCount = 0;
        this.previousBucketCount = 0;
    }

    @Override
    public synchronized boolean allow() {
        return allowRequest();
    }

    /**
     * Case 1: Normal bucket shift (one window passed)
     * now = 110 seconds
     * now >= currentBucketStartTime + windowSizeInMillis → 110 >= 100 + 10 → 110 >= 110 → true
     * bucketsToShift = (now - currentBucketStartTime) / windowSizeInMillis → (110 - 100) / 10 = 10 / 10 = 1
     * Since bucketsToShift == 1:
     * previousBucketCount = currentBucketCount → previousBucketCount = 5
     * Reset:
     * currentBucketCount = 0
     * currentBucketStartTime = now - (now % windowSizeInMillis) → 110 - (110 % 10) = 110 - 0 = 110
     *
     *
     * Case 2: Large time jump (multiple windows passed)
     * now = 135 seconds
     * now >= currentBucketStartTime + windowSizeInMillis → 135 >= 100 + 10 → 135 >= 110 → true
     * bucketsToShift = (now - currentBucketStartTime) / windowSizeInMillis → (135 - 100) / 10 = 35 / 10 = 3
     * Since bucketsToShift > 1:
     * previousBucketCount = 0 (all previous buckets are expired)
     * Reset:
     * currentBucketCount = 0
     * currentBucketStartTime = now - (now % windowSizeInMillis) → 135 - (135 % 10) = 135 - 5 = 130
     */
    public synchronized boolean allowRequest() {
        long now = clock.millis();
        // Step 1: Bucket shift
        if (now >= currentBucketStartTime + windowSizeInMillis) {
            long bucketsToShift = (now - currentBucketStartTime) / windowSizeInMillis;
            if (bucketsToShift == 1) {
                previousBucketCount = currentBucketCount;
            } else {
                previousBucketCount = 0; // Large time gap, all previous buckets expired
            }
            currentBucketCount = 0;
            currentBucketStartTime = now - (now % windowSizeInMillis);
        }
        // Step 2: Overlap calculation
        long timeIntoBucket = now - currentBucketStartTime;
        double overlapRatio = ((double) (windowSizeInMillis - timeIntoBucket)) / windowSizeInMillis;
        // Step 3: Effective count
        double effectiveCount = currentBucketCount + (previousBucketCount * overlapRatio);
        // Step 4: Apply rate limiting
        if (effectiveCount < maxRequests) {
            currentBucketCount++;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized RateLimitResult check() {
        boolean allowed = allowRequest();
        // retryAfter calculation is not strictly defined in the algorithm, so we return 0 for simplicity
        int remaining = Math.max(0, maxRequests - currentBucketCount);
        return new RateLimitResult(allowed, 0.0, remaining, maxRequests);
    }
}
