package com.example.ratelimiter.spring.web;

import com.example.ratelimiter.spring.resolver.KeyResolver;
import com.example.ratelimiter.core.registry.RateLimiterRegistry;
import com.example.ratelimiter.core.api.RateLimiter;
import com.example.ratelimiter.core.exception.RateLimitExceededException;
import com.example.ratelimiter.core.model.RateLimitResult;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RateLimitInterceptor implements HandlerInterceptor {
    private final KeyResolver keyResolver;
    private final RateLimiterRegistry registry;

    public RateLimitInterceptor(KeyResolver keyResolver, RateLimiterRegistry registry) {
        this.keyResolver = keyResolver;
        this.registry = registry;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String key = keyResolver.resolve(request);

        RateLimiter limiter = registry.getOrCreate(key);
        RateLimitResult result = limiter.check();

        if (!result.isAllowed()) {
            throw new RateLimitExceededException(
                    "Too many requests",
                    result.getRetryAfter()
            );
        }
        return true;
    }
}
