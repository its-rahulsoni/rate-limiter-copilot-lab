package com.example.ratelimiter.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {
    private int capacity;
    private double refillRate;
    private Boolean interceptorEnabled;
    private Boolean aspectEnabled;
    private String algorithm; // Remains String for YAML binding, but only for conversion to enum in config
    // ...other properties

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public double getRefillRate() { return refillRate; }
    public void setRefillRate(double refillRate) { this.refillRate = refillRate; }
    public Boolean getInterceptorEnabled() { return interceptorEnabled; }
    public void setInterceptorEnabled(Boolean interceptorEnabled) { this.interceptorEnabled = interceptorEnabled; }
    public Boolean getAspectEnabled() { return aspectEnabled; }
    public void setAspectEnabled(Boolean aspectEnabled) { this.aspectEnabled = aspectEnabled; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    // ...other getters/setters
}
