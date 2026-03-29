# EPIC: Rate Limiter Service (Token Bucket, In-Memory)

## Objective
Build a rate limiter using Token Bucket algorithm with Spring Boot.
The system should limit API requests per user/client.

---

## Functional Requirements

### FR1: Rate Limit API Requests
- Each user has a token bucket
- Bucket has capacity (max tokens)
- Tokens refill at fixed rate
- Each request consumes 1 token

### FR2: Reject Requests
- If no tokens available → reject request
- Return HTTP 429 (Too Many Requests)

### FR3: Configurable Limits
- Bucket size and refill rate should be configurable

---

## Non-Functional Requirements

- Thread-safe
- Low latency
- In-memory implementation using Guava Cache
- Clean architecture (Controller → Service → Core Logic)

---

## API Design

### Endpoint:
GET /api/resource

### Headers:
X-API-KEY: <user-id>

---

## Expected Behavior

- Allow requests within limit
- Reject excess requests
- Maintain per-user rate limiting

---

## LLD (Low Level Design)

### Components:

1. RateLimiterService
2. TokenBucket
3. TokenBucketManager (stores buckets per user using cache)
4. RateLimitFilter / Interceptor
5. Controller

---

## Testing Requirements

- Unit tests for TokenBucket
- Service tests with mocks
- Integration test for API