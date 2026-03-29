package com.example.ratelimiter.spring.resolver;

import jakarta.servlet.http.HttpServletRequest;

public interface KeyResolver {
    String resolve(HttpServletRequest request);
}
