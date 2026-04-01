package com.example.ratelimiter.core.algorithm;

import com.example.ratelimiter.core.config.RateLimiterConfig;
import com.example.ratelimiter.core.model.RateLimitResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FixedWindowRateLimiterTest {
    private RateLimiterConfig config;
    private Clock clock;
    private long baseTime;
    private static final int LIMIT = 5;
    private static final long WINDOW_SIZE = 1000L; // 1 second

    @BeforeEach
    void setUp() {
        baseTime = 1_000_000_000L;
        config = RateLimiterConfig.builder()
                .capacity(LIMIT)
                .refillRate(WINDOW_SIZE)
                .algorithm(null) // not used in this test
                .build();
        clock = mock(Clock.class);
        when(clock.millis()).thenReturn(baseTime);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(baseTime));
    }

    @Test
    void testNormalRequestFlowWithinLimit() {
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(config, clock);
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.allow(), "Request " + i + " should be allowed");
        }
    }

    @Test
    void testExceedingLimitWithinSameWindow() {
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(config, clock);
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.allow(), "Request " + i + " should be allowed");
        }
        assertFalse(limiter.allow(), "Request exceeding limit should be denied");
        RateLimitResult result = limiter.check();
        assertFalse(result.isAllowed());
        assertEquals(0, result.getRemainingTokens());
    }

    @Test
    void testResetBehaviorWhenWindowChanges() {
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(config, clock);
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.allow());
        }
        // Move to next window
        long nextWindowTime = baseTime + WINDOW_SIZE;
        when(clock.millis()).thenReturn(nextWindowTime);
        assertTrue(limiter.allow(), "First request in new window should be allowed");
    }

    @Test
    void testRetryAfterCorrectness() {
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(config, clock);
        for (int i = 0; i < LIMIT; i++) {
            limiter.allow();
        }
        // Denied request
        RateLimitResult result = limiter.check();
        assertFalse(result.isAllowed());
        double expectedRetry = (baseTime + WINDOW_SIZE - baseTime) / 1000.0;
        assertEquals(expectedRetry, result.getRetryAfter(), 0.0001);
    }

    @Test
    void testRequestAtWindowBoundary() {
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(config, clock);
        // Fill up the window
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.allow());
        }
        // Move to last ms of window
        long lastMs = baseTime + WINDOW_SIZE - 1;
        when(clock.millis()).thenReturn(lastMs);
        assertFalse(limiter.allow(), "Should be denied at last ms if limit reached");
        // Move to next window
        long nextWindow = baseTime + WINDOW_SIZE;
        when(clock.millis()).thenReturn(nextWindow);
        assertTrue(limiter.allow(), "Should be allowed in new window");
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(config, clock);
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
        assertEquals(LIMIT, allowed.get(), "No more than limit should be allowed concurrently");
    }
}
