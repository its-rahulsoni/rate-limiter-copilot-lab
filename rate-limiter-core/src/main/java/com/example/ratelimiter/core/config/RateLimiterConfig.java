package com.example.ratelimiter.core.config;

public class RateLimiterConfig {
    private final int capacity;
    private final double refillRate;

    private RateLimiterConfig(Builder builder) {
        this.capacity = builder.capacity;
        this.refillRate = builder.refillRate;
    }

    public int getCapacity() { return capacity; }
    public double getRefillRate() { return refillRate; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int capacity;
        private double refillRate;

        public Builder capacity(int capacity) {
            this.capacity = capacity;
            return this;
        }
        public Builder refillRate(double refillRate) {
            this.refillRate = refillRate;
            return this;
        }
        public RateLimiterConfig build() {
            return new RateLimiterConfig(this);
        }
    }
}
