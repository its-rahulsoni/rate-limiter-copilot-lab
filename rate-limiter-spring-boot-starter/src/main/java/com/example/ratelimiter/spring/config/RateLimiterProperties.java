package com.example.ratelimiter.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {
    private int capacity;
    private double refillRate;
    private Boolean interceptorEnabled;
    private Boolean aspectEnabled;
    private String strategy;
    // ...other properties

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public double getRefillRate() { return refillRate; }
    public void setRefillRate(double refillRate) { this.refillRate = refillRate; }
    public Boolean getInterceptorEnabled() { return interceptorEnabled; }
    public void setInterceptorEnabled(Boolean interceptorEnabled) { this.interceptorEnabled = interceptorEnabled; }
    public Boolean getAspectEnabled() { return aspectEnabled; }
    public void setAspectEnabled(Boolean aspectEnabled) { this.aspectEnabled = aspectEnabled; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
    // ...other getters/setters
}
