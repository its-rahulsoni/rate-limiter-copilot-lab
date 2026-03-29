package com.example.ratelimiter.core.exception;

public class RateLimitExceededException extends RuntimeException {
    private final double retryAfter;

    public RateLimitExceededException(String message, double retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }

    public double getRetryAfter() {
        return retryAfter;
    }
}
