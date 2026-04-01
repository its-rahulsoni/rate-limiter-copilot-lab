package com.example.ratelimiter.core.algorithm;

import com.example.ratelimiter.core.config.RateLimiterConfig;
import com.example.ratelimiter.core.model.RateLimitResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeakyBucketRateLimiterTest {
    private RateLimiterConfig config;
    private Clock clock;
    private long baseTime;
    private static final int CAPACITY = 3;
    private static final double LEAK_RATE = 1.0; // 1 request/sec

    @BeforeEach
    void setUp() {
        baseTime = 1_000_000_000L;
        config = RateLimiterConfig.builder()
                .capacity(CAPACITY)
                .refillRate(LEAK_RATE)
                .algorithm(null) // not used in this test
                .build();
        clock = mock(Clock.class);
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(baseTime));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    void testNormalRequestFlow() {
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(baseTime));
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(config, clock);
        for (int i = 0; i < CAPACITY; i++) {
            assertTrue(limiter.allow(), "Request " + i + " should be allowed");
        }
    }

    @Test
    void testRejectionWhenCapacityExceeded() {
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(baseTime));
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(config, clock);
        for (int i = 0; i < CAPACITY; i++) {
            assertTrue(limiter.allow());
        }
        assertFalse(limiter.allow(), "Should reject when bucket is full");
        RateLimitResult result = limiter.check();
        assertFalse(result.isAllowed());
        assertEquals(0, result.getRemainingTokens());
    }

    @Test
    void testLeakageOverTime() {
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(baseTime));
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(config, clock);
        for (int i = 0; i < CAPACITY; i++) {
            assertTrue(limiter.allow());
        }
        // Advance time by 2 seconds (should leak 2 tokens)
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(baseTime + 2000));
        assertTrue(limiter.allow(), "Should allow after leakage");
    }

    @Test
    void testRetryAfterCorrectness() {
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(baseTime));
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(config, clock);
        for (int i = 0; i < CAPACITY; i++) {
            limiter.allow();
        }
        // Denied request
        RateLimitResult result = limiter.check();
        assertFalse(result.isAllowed());
        // retryAfter = (currentWater - capacity + 1) / leakRate
        double expectedRetry = (1.0) / LEAK_RATE;
        assertEquals(expectedRetry, result.getRetryAfter(), 0.0001);
    }

    @Test
    void testEdgeCasesZeroWaterAndNearCapacity() {
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(baseTime));
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(config, clock);
        // Zero water: should allow
        assertTrue(limiter.allow());
        // Fill to near capacity
        assertTrue(limiter.allow());
        assertTrue(limiter.allow());
        // Now at capacity, next should be denied
        assertFalse(limiter.allow());
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(baseTime));
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(config, clock);
        int threads = 10;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger allowed = new AtomicInteger(0);
        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                if (limiter.allow()) {
                    allowed.incrementAndGet();
                }
                latch.countDown();
            }).start();
        }
        latch.await();
        assertEquals(CAPACITY, allowed.get(), "No more than capacity should be allowed concurrently");
    }
}
