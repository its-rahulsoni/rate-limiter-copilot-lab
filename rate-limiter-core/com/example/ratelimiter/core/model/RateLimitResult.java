package com.example.ratelimiter.core.model;

public class RateLimitResult {
    private final boolean allowed;
    private final long retryAfter;
    private final int remainingTokens;
    private final int limit;

    public RateLimitResult(boolean allowed, long retryAfter, int remainingTokens, int limit) {
        this.allowed = allowed;
        this.retryAfter = retryAfter;
        this.remainingTokens = remainingTokens;
        this.limit = limit;
    }

    public boolean isAllowed() { return allowed; }
    public long getRetryAfter() { return retryAfter; }
    public int getRemainingTokens() { return remainingTokens; }
    public int getLimit() { return limit; }
}
