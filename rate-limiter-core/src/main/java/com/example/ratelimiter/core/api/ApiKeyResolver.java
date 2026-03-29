package com.example.ratelimiter.core.api;

public interface ApiKeyResolver {
    String resolve(Object context);
}
