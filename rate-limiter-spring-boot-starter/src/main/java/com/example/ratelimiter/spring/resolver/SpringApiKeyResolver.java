package com.example.ratelimiter.spring.resolver;

import jakarta.servlet.http.HttpServletRequest;

public class SpringApiKeyResolver implements KeyResolver {
    @Override
    public String resolve(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-KEY");
        return (apiKey != null && !apiKey.isBlank()) ? apiKey : "anonymous";
    }
}
