# Rate Limiter Library - Architecture

## 1. Overview

This project implements a **reusable rate limiting library** for Java applications.

It supports:

* Manual usage (programmatic control)
* Interceptor-based usage (automatic HTTP rate limiting)
* Annotation-based usage (declarative method-level rate limiting)

The system is designed to be:

* Framework-agnostic (core module)
* Easily pluggable into Spring Boot applications
* Extensible for future algorithms and distributed systems

---

## 2. Design Principles

### Separation of Concerns

* Core rate limiting logic is independent of frameworks
* Spring-specific logic is isolated in a separate module

### Abstraction First Design

* Public `RateLimiter` interface for consistent usage
* Strategy pattern for algorithm flexibility

### Extensibility

* Support multiple algorithms (Token Bucket, Sliding Window, etc.)
* Pluggable key resolution and storage mechanisms

### Testability

* Time abstraction using `java.time.Clock`
* Clean separation enables unit and integration testing

---

## 3. Module Structure

### 3.1 rate-limiter-core

**Purpose:**
Pure Java module containing all core rate limiting logic.
No dependency on Spring or web frameworks.

---

### Package Structure

com.example.ratelimiter.core

* api
* strategy
* model
* registry
* factory
* exception

---

### Key Components

#### api

* `RateLimiter` (interface)

  * Represents a **per-key rate limiter instance (stateful)**
  * Methods:

    * `boolean allow()`
    * `RateLimitResult check()`

---

#### strategy

* `RateLimitingStrategy` (interface)

  * Defines algorithm behavior
  * Does NOT manage keys

* Implementations:

  * `TokenBucketStrategy`
  * `LeakyBucketStrategy`
  * `FixedWindowStrategy`

---

#### model

* `RateLimitResult`

  * Fields:

    * allowed
    * retryAfter
    * remainingTokens
    * limit

* `RateLimiterConfig`

  * Configuration object (builder pattern)

---

#### registry

* `RateLimiterRegistry`

  * Manages per-key `RateLimiter` instances
  * Uses Guava Cache (in-memory)
  * Responsible for lifecycle of rate limiters

---

#### factory

* `RateLimiterFactory`

  * Creates `RateLimiter` instances
  * Injects:

    * strategy
    * config
    * Clock

---

#### exception

* `RateLimitExceededException`

---

### Design Rules

* Each key has its own `RateLimiter` instance
* `RateLimiter` holds state (tokens, timestamps)
* `RateLimitingStrategy` contains only algorithm logic
* All time handling must use `java.time.Clock`
* Implementation must be thread-safe
* Lazy refill strategy for token bucket

---

### 3.2 rate-limiter-spring-boot-starter

**Purpose:**
Provides Spring Boot integration and auto-configuration.

#### Package: `com.example.ratelimiter.spring`

**Key Components:**

* `RateLimiterAutoConfiguration`
  Auto-configures beans using `@ConditionalOnMissingBean`

* `RateLimiterProperties`
  Binds configuration from `application.yml`

* `RateLimitInterceptor`
  Handles automatic HTTP rate limiting

* `RateLimitAspect`
  Enables annotation-based rate limiting (AOP)

* `@EnableRateLimiting`
  Enables library via annotation

* `@RateLimited`
  Declarative rate limiting

* `RateLimitExceptionHandler`
  Global exception handling (`@ControllerAdvice`)

* `SpringApiKeyResolver`
  Extracts API key from HTTP requests

---

## 4. Request Flow

### 4.1 Interceptor-Based Flow

HTTP Request
→ RateLimitInterceptor
→ SpringApiKeyResolver
→ RateLimiter
→ RateLimitingStrategy (Token Bucket)
→ RateLimitResult
→ Allow / Reject (HTTP 429)

---

### 4.2 Annotation-Based Flow

HTTP Request
→ Controller Method
→ RateLimitAspect (AOP)
→ RateLimiter
→ RateLimitingStrategy
→ Allow / Reject

---

### 4.3 Manual Usage Flow

Application Code
→ RateLimiter.allow(key)
→ Strategy Execution
→ Result (allowed / rejected)

---

## 5. Usage Modes

### 5.1 Manual Usage

```java
RateLimiter limiter = new TokenBucketRateLimiter(config);

if (limiter.allow(apiKey)) {
    // proceed
} else {
    // handle rejection
}
```

---

### 5.2 Annotation-Based Usage

```java
@RateLimited
@GetMapping("/api/resource")
public ResponseEntity<?> getResource() {
    return ResponseEntity.ok("success");
}
```

---

### 5.3 Interceptor-Based Usage

* Automatically applies rate limiting to incoming HTTP requests
* No changes required in controller logic

---

## 6. Configuration

Example:

```yaml
rate-limiter:
  capacity: 10
  refill-rate: 5
```

---

## 7. Design Decisions

* Token Bucket algorithm with **lazy refill**
* In-memory storage using **Guava Cache**
* Thread safety ensured via controlled synchronization
* Use of `Clock` for time abstraction and testability
* Clean architecture with layered separation

---

## 8. Extensibility

The system supports:

* Adding new algorithms via `RateLimitingStrategy`
* Custom key resolution via `ApiKeyResolver`
* Future distributed storage (e.g., Redis)
* Custom rate limiting policies

---

## 9. Future Enhancements

* Redis-based distributed rate limiter
* Sliding window algorithm
* Metrics and observability integration
* Dynamic configuration updates
* Advanced annotation parameters

---

## 10. Non-Functional Considerations

* Thread-safe design
* Low latency (in-memory operations)
* Configurable and extensible
* Suitable for high-throughput APIs
