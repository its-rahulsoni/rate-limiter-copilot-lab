package com.example.ratelimiter.spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.ratelimiter.spring.resolver.KeyResolver;
import com.example.ratelimiter.spring.resolver.SpringApiKeyResolver;
import com.example.ratelimiter.core.registry.RateLimiterRegistry;
import com.example.ratelimiter.core.factory.RateLimiterFactory;
import com.example.ratelimiter.spring.web.RateLimitInterceptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.example.ratelimiter.core.strategy.RateLimitingStrategy;
import com.example.ratelimiter.core.strategy.TokenBucketStrategy;
import com.example.ratelimiter.core.config.RateLimiterConfig;
import java.time.Clock;

@Configuration
@EnableConfigurationProperties(RateLimiterProperties.class)
public class RateLimiterAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public KeyResolver keyResolver() {
        return new SpringApiKeyResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimiterRegistry rateLimiterRegistry(RateLimiterFactory factory) {
        return new RateLimiterRegistry(factory);
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimiterFactory rateLimiterFactory(RateLimiterProperties properties) {
        RateLimiterConfig config = RateLimiterConfig.builder()
                .capacity(properties.getCapacity())
                .refillRate(properties.getRefillRate())
                .build();
        String strategyType = properties.getStrategy();
        RateLimitingStrategy strategy;
        if ("token-bucket".equalsIgnoreCase(strategyType) || strategyType == null) {
            strategy = new TokenBucketStrategy(config, Clock.systemUTC());
        } else {
            throw new IllegalArgumentException("Unsupported rate limiting strategy: " + strategyType);
        }
        return new RateLimiterFactory(strategy, config, Clock.systemUTC());
    }

    @Bean
    @ConditionalOnProperty(prefix = "rate-limiter", name = "interceptor-enabled", havingValue = "true", matchIfMissing = true)
    public WebMvcConfigurer webMvcConfig(KeyResolver keyResolver, RateLimiterRegistry rateLimiterRegistry) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
                registry.addInterceptor(new RateLimitInterceptor(keyResolver, rateLimiterRegistry));
            }
        };
    }
}
