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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SlidingWindowCounterRateLimiterTest {
    private RateLimiterConfig config;
    private Clock clock;
    private long baseTime;
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_SIZE = 1000L; // 1 second

    @BeforeEach
    void setUp() {
        baseTime = 1_000_000_000L;
        config = RateLimiterConfig.builder()
                .capacity(MAX_REQUESTS)
                .refillRate(WINDOW_SIZE)
                .algorithm(null) // not used in this test
                .build();
        clock = mock(Clock.class);
        when(clock.millis()).thenReturn(baseTime);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    void testNormalRequestFlow() {
        when(clock.millis()).thenReturn(baseTime);
        SlidingWindowCounterRateLimiter limiter = new SlidingWindowCounterRateLimiter(config, clock);
        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(limiter.allowRequest(), "Request " + i + " should be allowed");
        }
    }

    @Test
    void testRejectionWhenLimitExceeded() {
        when(clock.millis()).thenReturn(baseTime);
        SlidingWindowCounterRateLimiter limiter = new SlidingWindowCounterRateLimiter(config, clock);
        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(limiter.allowRequest());
        }
        assertFalse(limiter.allowRequest(), "Should reject when limit exceeded");
        RateLimitResult result = limiter.check();
        assertFalse(result.isAllowed());
        assertEquals(0, result.getRemainingTokens());
    }


    @Test
    void testBucketShiftScenarios() {
        SlidingWindowCounterRateLimiter limiter = new SlidingWindowCounterRateLimiter(config, clock);
        // Fill up the window
        when(clock.millis()).thenReturn(baseTime);
        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(limiter.allowRequest());
        }
        // Large time jump (multiple windows)
        long farFuture = baseTime + 10 * WINDOW_SIZE;
        when(clock.millis()).thenReturn(farFuture);
        assertTrue(limiter.allowRequest(), "Should allow after large time jump (buckets reset)");
    }

    @Test
    void testBoundaryConditions() {
        SlidingWindowCounterRateLimiter limiter = new SlidingWindowCounterRateLimiter(config, clock);
        // At the very start of a window
        when(clock.millis()).thenReturn(baseTime);
        assertTrue(limiter.allowRequest());
        // At the very end of a window
        when(clock.millis()).thenReturn(baseTime + WINDOW_SIZE - 1);
        assertTrue(limiter.allowRequest());
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        when(clock.millis()).thenReturn(baseTime);
        SlidingWindowCounterRateLimiter limiter = new SlidingWindowCounterRateLimiter(config, clock);
        int threads = 20;
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger allowed = new AtomicInteger(0);
        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                if (limiter.allowRequest()) {
                    allowed.incrementAndGet();
                }
                latch.countDown();
            }).start();
        }
        latch.await();
        assertEquals(MAX_REQUESTS, allowed.get(), "No more than maxRequests should be allowed concurrently");
    }

    @Test
    void testStressScenario() throws InterruptedException {
        when(clock.millis()).thenReturn(baseTime);
        SlidingWindowCounterRateLimiter limiter = new SlidingWindowCounterRateLimiter(config, clock);
        int threads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        AtomicInteger allowed = new AtomicInteger(0);
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                if (limiter.allowRequest()) {
                    allowed.incrementAndGet();
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
        assertEquals(MAX_REQUESTS, allowed.get(), "No more than maxRequests should be allowed under stress");
    }
}
