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
Acts as an adapter layer between Spring Boot applications and the core rate-limiter engine.

This module:

* integrates with HTTP request lifecycle
* extracts client identity
* invokes core rate limiting logic
* converts results into HTTP responses

It MUST NOT contain any rate limiting algorithm logic.

---

### Package Structure

com.example.ratelimiter.spring

* config
* web
* aspect
* annotation
* resolver
* exception

---

### Key Components

---

#### config

##### `RateLimiterAutoConfiguration`

* Configures all required beans
* Uses `@ConditionalOnMissingBean` for extensibility
* Wires:

  * `RateLimiterRegistry`
  * `RateLimiterFactory`
  * `KeyResolver`

---

##### `RateLimiterProperties`

* Maps configuration from `application.yml`
* Example:

```yaml
rate-limiter:
  capacity: 10
  refill-rate: 5
```

---

#### resolver

##### `KeyResolver` (interface)

* Extracts unique client key
* Default implementation: `SpringApiKeyResolver`

---

##### `SpringApiKeyResolver`

* Extracts API key from HTTP request
* Default: `X-API-KEY` header
* Fallback strategy allowed

---

#### web

##### `RateLimitInterceptor`

* Entry point for HTTP requests
* Runs BEFORE controller execution

---

### Flow:

1. Intercept incoming request
2. Extract key using `KeyResolver`
3. Get `RateLimiter` from `RateLimiterRegistry`
4. Call `rateLimiter.check()`
5. If allowed → continue request
6. If rejected → throw `RateLimitExceededException`

---

#### aspect

##### `RateLimitAspect`

* Handles annotation-based rate limiting
* Intercepts method calls annotated with `@RateLimited`

---

### Flow:

1. Intercept annotated method
2. Resolve key (via KeyResolver or annotation config)
3. Call `RateLimiter`
4. Allow or throw exception

---

#### annotation

##### `@EnableRateLimiting`

* Enables auto-configuration
* Registers interceptor and aspect

---

##### `@RateLimited`

* Marks methods for rate limiting
* Default behavior:

  * uses global configuration
* Advanced parameters optional (future enhancement)

---

#### exception

##### `RateLimitExceptionHandler`

* Global exception handler (`@ControllerAdvice`)
* Converts `RateLimitExceededException` to HTTP response

---

### Response Example:

```json
{
  "error": "Too many requests",
  "retryAfter": 10
}
```

---

### Design Rules

* This module MUST NOT implement rate limiting logic
* It MUST delegate all decisions to core module
* Interceptor handles HTTP-level rate limiting
* Aspect handles method-level rate limiting
* Exception handling must be centralized
* Key extraction must be pluggable


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

## 11. Rate Limiting Algorithms (Detailed Design)

This section defines the exact implementation logic for rate limiting strategies.
All strategies must follow these rules:

### Common Rules

* Each `RateLimiter` instance is per-key (stateful)
* All time must be derived using `java.time.Clock`
* All implementations must be thread-safe
* Algorithms must use **lazy evaluation** (no background threads)
* Return `RateLimitResult` with:

  * allowed
  * retryAfter (in seconds)
  * remainingTokens
  * limit

---

# 11.1 Token Bucket Strategy

## Concept

* A bucket holds tokens up to a maximum capacity
* Each request consumes 1 token
* Tokens are refilled over time at a fixed rate

---

## State Variables

* capacity (max tokens)
* tokens (current tokens)
* refillRate (tokens per second)
* lastRefillTime (timestamp)

---

## Algorithm

1. Get current time from Clock

2. Calculate time elapsed since last refill:

   elapsedTime = now - lastRefillTime

3. Calculate tokens to add:

   tokensToAdd = elapsedTime * refillRate

4. Update tokens:

   tokens = min(capacity, tokens + tokensToAdd)

5. Update lastRefillTime = now

6. If tokens > 0:

  * tokens--
  * allowed = true

7. Else:

  * allowed = false

---

## retryAfter Calculation

If request is rejected:

retryAfter = time required to generate 1 token

retryAfter = 1 / refillRate (in seconds)

---

## Edge Cases

* Ensure tokens never exceed capacity
* Handle fractional refill precisely
* Prevent race conditions (use lock or atomic updates)
* Handle system clock drift safely
* High concurrency must not oversell tokens

---

---

# 11.2 Fixed Window Strategy

## Concept

* Requests are counted within a fixed time window
* Example: 10 requests per minute
* Counter resets when window changes

---

## State Variables

* maxRequests
* currentWindowStartTime
* requestCount
* windowSize (in seconds)

---

## Algorithm

1. Get current time

2. Determine current window:

   windowStart = floor(now / windowSize) * windowSize

3. If windowStart != currentWindowStartTime:

  * reset requestCount = 0
  * update currentWindowStartTime

4. If requestCount < maxRequests:

  * requestCount++
  * allowed = true

5. Else:

  * allowed = false

---

## retryAfter Calculation

retryAfter = time until next window

retryAfter = (currentWindowStartTime + windowSize) - now

---

## Edge Cases

* Boundary burst problem (users can send double requests at window edges)
* Ensure correct window reset
* Thread safety for counter updates

---

---

# 11.3 Leaky Bucket Strategy

## Concept

* Requests are processed at a fixed rate
* Excess requests are queued or rejected
* Smooths traffic

---

## State Variables

* capacity (max queue size)
* leakRate (requests per second)
* currentWaterLevel
* lastLeakTime

---

## Algorithm

1. Get current time

2. Calculate leaked requests:

   elapsedTime = now - lastLeakTime
   leaked = elapsedTime * leakRate

3. Reduce water level:

   currentWaterLevel = max(0, currentWaterLevel - leaked)

4. Update lastLeakTime = now

5. If currentWaterLevel < capacity:

  * currentWaterLevel++
  * allowed = true

6. Else:

  * allowed = false

---

## retryAfter Calculation

retryAfter = time until one slot frees

retryAfter = 1 / leakRate

---

## Edge Cases

* Prevent negative water level
* Handle burst smoothing correctly
* Maintain precision in leak calculations
* Thread safety required

---

---

# 11.4 Strategy Selection

* Default strategy: Token Bucket
* Strategy should be configurable via properties
* Factory must create appropriate implementation based on configuration

---

# 11.5 Performance Considerations

* Avoid heavy locking
* Use minimal synchronization
* Prefer atomic operations where possible
* Ensure O(1) operations per request

---
