# Rate Limiter Library – Algorithms

This document defines implementation specifications for rate limiting algorithms.
Each section provides clear, structured instructions for GitHub Copilot to generate correct implementations.

---

## 1. Fixed Window Rate Limiter

### 📌 Objective

Implement the `FixedWindowRateLimiter` class using the Fixed Window rate limiting algorithm.

---

### 🧩 Interface Contract

The class must implement:

```java
boolean allow();
RateLimitResult check();
```

---

### ⚙️ Configuration

Use the following fields from `RateLimiterConfig`:

* `limit` → maximum requests allowed per window
* `windowSize` → duration of window (in milliseconds)

---

### 🧠 Internal State

Maintain the following variables:

* `currentWindowId` (long)
* `requestCount` (int)

---

### ⏱ Time Handling

* Use `java.time.Clock`
* Retrieve time using:

```java
long currentTimeMillis = clock.millis();
```

---

### 🧮 Window Calculation

```java
currentWindowId = currentTimeMillis / windowSize;
```

---

### 🔁 Core Logic

1. **Check window transition**

   If:

   ```text
   currentWindowId != previousWindowId
   ```

   Then:

    * reset `requestCount = 0`
    * update `currentWindowId`

---

2. **Apply rate limit**

    * If `requestCount < limit`:

        * increment `requestCount`
        * allow request

    * Else:

        * reject request

---

### ⏳ retryAfter Calculation

When request is rejected:

```java
long windowEnd = (currentWindowId + 1) * windowSize;
double retryAfter = (windowEnd - currentTimeMillis) / 1000.0;
```

---

### 🔒 Thread Safety

* Use `ReentrantLock`
* Ensure all state updates are thread-safe

---

### 📤 Response

Return a `RateLimitResult` containing:

* `allowed`
* `retryAfter`
* `remainingRequests = limit - requestCount`
* `limit`

---

### 🚫 Constraints

* Do NOT modify existing interfaces
* Do NOT change `RateLimiterConfig`
* Do NOT use external libraries

---

### ✅ Expected Outcome

The implementation should:

* correctly enforce request limits per window
* reset counters on window boundaries
* compute accurate `retryAfter` values
* be thread-safe and production-ready

---

### 🔍 Post-Implementation Validation (for Copilot)

After implementation, verify:

1. Window calculation is correct
2. No off-by-one errors exist
3. `retryAfter` is accurate
4. Thread safety is properly handled
5. Code aligns with FixedWindowAlgorithm.md

---

## 1.1 ## 🔧 Copilot Implementation & Validation Workflow (Fixed Window)

This section defines how to use GitHub Copilot to implement, validate, and test the Fixed Window Rate Limiter.

---

### 🟢 Step 1: Implementation Prompt

Use the following prompt to generate the implementation:

```text
Read the ALGORITHMS.md file and implement section "1. Fixed Window Rate Limiter".

Follow all instructions strictly, including:
- interface contract (RateLimiter)
- configuration usage (limit, windowSize)
- state variables (currentWindowId, requestCount)
- time handling using Clock
- retryAfter calculation
- thread safety using ReentrantLock

Do not change any existing interfaces or configs.
Ensure the implementation is clean, readable, and production-ready.
```

---

### 🟡 Step 2: Force Explanation (Critical)

After code generation, ensure Copilot explains the implementation:

```text
Explain the FixedWindowRateLimiter implementation step by step and map each part of the code to the corresponding section in ALGORITHMS.md.
```

---

### 🔍 Step 3: Validation Prompt

Validate correctness of the implementation:

```text
Review the FixedWindowRateLimiter implementation and verify:

1. Window calculation is correct (currentTime / windowSize).
2. Window reset logic works correctly.
3. No off-by-one errors in request counting.
4. retryAfter calculation is accurate.
5. remainingRequests is correct.
6. Thread safety is properly implemented using ReentrantLock.
7. No race conditions exist.
8. Code aligns strictly with ALGORITHMS.md.

Highlight any issues and suggest fixes.
```

---

### ⚠️ Step 4: Edge Case Analysis (Most Important)

Analyze robustness of the implementation:

```text
Analyze the FixedWindowRateLimiter implementation for edge cases.

Specifically check:

1. Behavior at window boundary (e.g., request at last ms of window).
2. Burst requests across window boundaries.
3. limit = 0 scenario.
4. windowSize = 0 or invalid values.
5. High concurrency scenarios.
6. Time overflow or long idle periods.
7. Correct reset of requestCount when window changes.

Suggest improvements if any issue is found.
```

---

### 🧪 Step 5: Unit Test Generation

Generate comprehensive test coverage:

```text
Generate unit tests for FixedWindowRateLimiter covering:

1. Normal request flow within limit.
2. Exceeding limit within same window.
3. Reset behavior when window changes.
4. retryAfter correctness.
5. Edge case: request at window boundary.
6. Concurrent access scenarios.

Use JUnit and Mockito where applicable.
```

---

### 🧠 Step 6: Manual Sanity Checklist

Perform the following checks manually:

* Window calculation:

   * `windowId = currentTime / windowSize`

* Reset logic:

   * requestCount resets when window changes

* Off-by-one correctness:

   * exactly `limit` requests allowed
   * next request rejected

* retryAfter:

   * positive when rejected
   * zero when allowed

* Remaining requests:

   * `remaining = limit - requestCount`

---

### 🚨 Common Bugs to Watch

* Allowing `limit + 1` requests (off-by-one error)
* Negative or incorrect `retryAfter`
* Window not resetting properly
* Race conditions due to missing locking
* Incorrect time unit usage (seconds vs milliseconds)

---

### 🔁 Recommended Workflow

Follow this loop for reliable implementation:

1. Define spec in `ALGORITHMS.md`
2. Generate code using Copilot
3. Ask Copilot to explain implementation
4. Validate using structured prompts
5. Perform manual sanity checks
6. Add unit tests

---

### 🎯 Key Insight

This approach ensures:

* Spec-driven development
* Deterministic AI behavior
* Production-quality implementations

---------------------------------------------------
---------------------------------------------------

## 2. Token Bucket Rate Limiter

## 🔧 Copilot Implementation & Validation Workflow (Token Bucket)

---

# 🟢 Phase 1: Implementation Prompt

```text
Implement a thread-safe TokenBucketRateLimiter class based on the Token Bucket rate limiting algorithm.

Requirements:

1. The class must implement the RateLimiter interface:
   - boolean allow()
   - RateLimitResult check()

2. Use RateLimiterConfig:
   - capacity (max tokens)
   - refillRate (tokens per second)

3. Maintain the following state:
   - tokens (double)
   - lastRefillTime (Instant)

4. Use java.time.Clock for time handling.

5. Refill logic (lazy refill):
   - elapsedTime = now - lastRefillTime
   - tokensToAdd = elapsedTime × refillRate
   - tokens = min(capacity, tokens + tokensToAdd)
   - update lastRefillTime

6. Allow logic:
   - if tokens ≥ 1 → allow and decrement token
   - else → reject

7. retryAfter calculation:
   - tokensNeeded = 1 - currentTokens
   - retryAfter = tokensNeeded / refillRate

8. Ensure thread safety using ReentrantLock.

9. Return RateLimitResult:
   - allowed
   - retryAfter
   - remainingTokens
   - capacity

10. Use fractional tokens (double precision).

11. Do not use any external libraries.

Ensure clean, readable, production-quality Java code.
```

---

### 🟡 Step 2: Force Explanation (Critical)

```text
Explain the TokenBucketRateLimiter implementation step by step and map each part of the code to the TokenBucketAlgorithm.md specification.
```

---

### 🔍 Step 3: Validation Prompt

```text
Review the TokenBucketRateLimiter implementation and verify:

1. Continuous refill logic is correct (elapsedTime × refillRate).
2. Tokens never exceed capacity.
3. lastRefillTime is updated correctly (no time drift).
4. Fractional tokens are handled properly.
5. retryAfter calculation is accurate.
6. Thread safety is ensured using ReentrantLock.
7. No race conditions exist.
8. Code aligns with TokenBucketAlgorithm.md.

Highlight any issues and suggest fixes.
```

---

### ⚠️ Step 4: Edge Case Analysis

```text
Analyze the TokenBucketRateLimiter implementation for edge cases.

Specifically check:

1. tokens close to 1 (e.g., 0.9 scenario).
2. tokens = 0 scenario.
3. refillRate = 0 (division by zero).
4. long idle periods (large elapsed time).
5. high concurrency scenarios.
6. floating point precision issues.
7. token overflow beyond capacity.

Suggest improvements if any issue is found.
```

---

### 🧪 Step 5: Unit Test Generation

```text
Generate unit tests for TokenBucketRateLimiter covering:

1. Normal request flow within capacity.
2. Token depletion and rejection.
3. Token refill over time.
4. retryAfter correctness.
5. Fractional token behavior.
6. Edge cases (low tokens, refill timing).
7. Concurrent access scenarios.

Use JUnit and Mockito where applicable.
```

---

### 🧠 Step 6: Manual Sanity Checklist

* Refill logic:

    * `tokensToAdd = elapsedTime × refillRate`

* Token cap:

    * tokens ≤ capacity

* Time update:

    * lastRefillTime = now

* Allow logic:

    * tokens ≥ 1 → allow
    * tokens < 1 → reject

* retryAfter:

    * `(1 - tokens) / refillRate`

---

### 🚨 Common Bugs to Watch

* Incorrect lastRefillTime update (time drift bug)
* Token overflow beyond capacity
* Incorrect retryAfter (ignoring fractional tokens)
* Division by zero when refillRate = 0
* Race conditions due to missing locking
* Loss of precision due to integer math

---

### 🔁 Recommended Workflow

1. Enum migration (Phase 0)
2. Algorithm spec review
3. Implementation via Copilot
4. Explanation mapping
5. Validation prompts
6. Edge case analysis
7. Manual sanity checks
8. Unit testing

---

### 🎯 Key Insight

This workflow ensures:

* Type-safe configuration (enum-based)
* Correct algorithm implementation
* Deterministic AI behavior
* Production-grade reliability

---

# 🧠 Phase 0: Algorithm Configuration Refactor (Enum Migration)

### 📌 Goal

Convert:

```text
algorithm: "token-bucket" ❌
```

into:

```text
algorithm: AlgorithmType.TOKEN_BUCKET ✅
```

---

### 🟢 Step 0.1 — Create Enum

```text
Create an enum AlgorithmType in the core module under config package.

It should contain:
- TOKEN_BUCKET
- FIXED_WINDOW
- LEAKY_BUCKET

Ensure naming follows Java enum conventions and is uppercase.
```

---

### 🟢 Step 0.2 — Update Config Class

```text
Update RateLimiterConfig to replace the algorithm field from String to AlgorithmType.

Make the following changes:
1. Replace the existing algorithm field with AlgorithmType.
2. Update the builder pattern to accept AlgorithmType.
3. Update getter to return AlgorithmType.
4. Ensure immutability is preserved.
```

---

### 🟢 Step 0.3 — Update Properties Mapping

```text
Update RateLimiterProperties to support algorithm as a String input from application.yml.

Do not change the YAML format, but ensure this String can be safely converted to AlgorithmType in configuration layer.
```

---

### 🟢 Step 0.4 — Update AutoConfiguration (MOST IMPORTANT)

```text
Update RateLimiterAutoConfiguration to convert the String property "algorithm" into AlgorithmType enum.

Requirements:
1. Convert using valueOf with toUpperCase().
2. Provide default value TOKEN_BUCKET if property is null.
3. Handle invalid values by throwing a clear IllegalArgumentException.
4. Pass AlgorithmType to RateLimiterConfig builder.
```

---

### 🟢 Step 0.5 — Update Factory

```text
Update RateLimiterFactory to use AlgorithmType enum instead of String.

Requirements:
1. Replace all string comparisons with switch-case on AlgorithmType.
2. Ensure each enum maps to correct RateLimiter implementation:
   - TOKEN_BUCKET → TokenBucketRateLimiter
   - FIXED_WINDOW → FixedWindowRateLimiter
   - LEAKY_BUCKET → LeakyBucketRateLimiter
3. Throw IllegalArgumentException for unsupported values.
```

---

### 🟢 Step 0.6 — Clean Up Old Code

```text
Remove all String-based algorithm handling across the codebase.

Ensure:
1. No string comparisons like equalsIgnoreCase remain.
2. No leftover "strategy" or "algorithm" string logic exists.
3. All usages rely on AlgorithmType enum.
```

---

### 🔍 Step 0.7 — Validation Prompt (Critical)

```text
Review the entire codebase and verify:

1. AlgorithmType enum is used consistently.
2. No string-based algorithm logic remains.
3. Factory correctly creates RateLimiter based on enum.
4. Application compiles without warnings or errors.
5. Default behavior works when algorithm is not specified.
```

---------------------------
---------------------------

