package com.example.ratelimiter.core.factory;

import com.example.ratelimiter.core.api.RateLimiter;
import com.example.ratelimiter.core.strategy.TokenBucketRateLimiter;
import com.example.ratelimiter.core.config.RateLimiterConfig;
import com.example.ratelimiter.core.config.AlgorithmType;
// import com.example.ratelimiter.core.strategy.FixedWindowRateLimiter;
// import com.example.ratelimiter.core.strategy.LeakyBucketRateLimiter;
import java.time.Clock;

public class RateLimiterFactory {
    private final RateLimiterConfig config;
    private final Clock clock;

    public RateLimiterFactory(RateLimiterConfig config, Clock clock) {
        this.config = config;
        this.clock = clock;
    }

    public RateLimiter createRateLimiter() {
        AlgorithmType algorithm = config.getAlgorithm();
        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm must be specified");
        }
        switch (algorithm) {
            case TOKEN_BUCKET:
                return new TokenBucketRateLimiter(config, clock);
            case FIXED_WINDOW:
                // return new FixedWindowRateLimiter(config, clock); // Uncomment when implemented
                throw new UnsupportedOperationException("FixedWindowRateLimiter not implemented yet");
            case LEAKY_BUCKET:
                // return new LeakyBucketRateLimiter(config, clock); // Uncomment when implemented
                throw new UnsupportedOperationException("LeakyBucketRateLimiter not implemented yet");
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
    }
}
