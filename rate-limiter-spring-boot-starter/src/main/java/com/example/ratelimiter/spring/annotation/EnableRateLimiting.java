package com.example.ratelimiter.spring.annotation;

import org.springframework.context.annotation.Import;
import com.example.ratelimiter.spring.config.RateLimiterAutoConfiguration;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RateLimiterAutoConfiguration.class)
public @interface EnableRateLimiting {
}
