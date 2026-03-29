package com.example.ratelimiter.spring.config;

import com.example.ratelimiter.spring.web.RateLimitInterceptor;
import com.example.ratelimiter.spring.resolver.KeyResolver;
import com.example.ratelimiter.core.registry.RateLimiterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final KeyResolver keyResolver;
    private final RateLimiterRegistry rateLimiterRegistry;

    @Autowired
    public WebMvcConfig(KeyResolver keyResolver, RateLimiterRegistry rateLimiterRegistry) {
        this.keyResolver = keyResolver;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(keyResolver, rateLimiterRegistry));
    }
}
