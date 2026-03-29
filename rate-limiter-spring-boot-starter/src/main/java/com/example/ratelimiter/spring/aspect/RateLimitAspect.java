package com.example.ratelimiter.spring.aspect;

import com.example.ratelimiter.core.model.RateLimitResult;
import com.example.ratelimiter.spring.resolver.KeyResolver;
import com.example.ratelimiter.core.registry.RateLimiterRegistry;
import com.example.ratelimiter.core.api.RateLimiter;
import com.example.ratelimiter.core.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class RateLimitAspect {
    @Autowired
    private KeyResolver keyResolver;
    @Autowired
    private RateLimiterRegistry registry;

    @Around("@annotation(com.example.ratelimiter.spring.annotation.RateLimited)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes())
                        .getRequest();

        String key = keyResolver.resolve(request);

        RateLimiter limiter = registry.getOrCreate(key);
        RateLimitResult result = limiter.check();

        if (!result.isAllowed()) {
            throw new RateLimitExceededException(
                    "Too many requests",
                    result.getRetryAfter()
            );
        }

        return pjp.proceed();
    }
}
