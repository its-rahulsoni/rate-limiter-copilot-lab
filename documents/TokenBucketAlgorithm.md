# Token Bucket Algorithm

---

## 🧠 1. Core Idea (Intuition First)

👉 Token Bucket controls **how fast requests are allowed over time**

---

### 🔥 Think like this:

You have a bucket filled with tokens.
Each request needs 1 token.

---

### Example:

Bucket capacity = 5

Tokens initially = 5

---

## 🟢 Request Flow

Request arrives:

* if token available → allow ✅
* else → reject ❌

---

## 🧠 2. Key Concept: Refill Over Time

This is what makes Token Bucket powerful.

---

### 🔹 Tokens regenerate

Refill rate = 2 tokens per second

---

### Example Timeline:

t = 0 → 5 tokens

t = 1 → +2 tokens (max 5)

t = 2 → +2 tokens

---

👉 Bucket never exceeds capacity

---

## ❗ Why Do We Multiply?

Formula:

> tokensToAdd = elapsedTime × refillRate

---

### 🧠 Explanation

You might think:

> “Why not just check if 1 second passed?”

That would be a **step-based approach**:

```
if (elapsedTime >= 1 sec)
    add tokens
```

---

### ❌ Problem with Step-Based Approach

* 0.9 sec → NO refill ❌
* 1.0 sec → sudden refill ✔

👉 This creates uneven / jerky behavior.

---

### ✅ Continuous Refill (Correct Approach)

> tokensToAdd = elapsedTime × refillRate

---

### Example:

refillRate = 2 tokens/sec

* after 1 sec → 2 tokens
* after 0.5 sec → 1 token
* after 0.25 sec → 0.5 token

---

👉 Tokens grow **smoothly over time**

---

### 🧠 Units Explanation

elapsedTime → seconds

refillRate → tokens/sec

So:

> tokensToAdd = seconds × (tokens/sec) = tokens

---

### 💡 Analogy

Like water filling a bucket:

* flow rate = 2 liters/sec
* time = 0.5 sec

water added = 1 liter

👉 Continuous, not step-based

---

## 🧠 3. Why Token Bucket is Used

✔ Handles bursts

User can send multiple requests instantly (burst)

✔ Then rate limits

After burst → controlled by refill rate

---

## 🧠 4. Core Variables

* capacity → max tokens
* tokens → current tokens
* refillRate → tokens per second
* lastRefillTime → last refill timestamp

---

## 🧠 5. Core Logic (Step-by-Step)

---

### Step 1: Calculate time passed

> elapsedTime = now - lastRefillTime

---

### Step 2: Add tokens

> tokensToAdd = elapsedTime × refillRate

> tokens = min(capacity, tokens + tokensToAdd)

---

### Step 3: Update time

lastRefillTime = now

---

### Step 4: Check availability

if tokens ≥ 1:

* tokens--
* allow

else:

* reject

---

## 🧠 6. retryAfter Concept

When request is rejected:

tokensNeeded = 1 - currentTokens

retryAfter = tokensNeeded / refillRate

---

### 🧠 Explanation

We want to know:

> “How long until 1 full token is available?”

---

### Example:

currentTokens = 0.3

refillRate = 2 tokens/sec

---

tokensNeeded = 1 - 0.3 = 0.7

retryAfter = 0.7 / 2 = 0.35 seconds

---

👉 After 0.35 sec → request will succeed ✅

---

### 🧠 Why Division?

tokensNeeded → tokens

refillRate → tokens/sec

So:

> (tokens) / (tokens/sec) = seconds

---

### ❌ Common Mistake

retryAfter = 1 / refillRate

👉 This assumes tokens = 0 always (incorrect)

---

### Example:

tokens = 0.9

Correct:

retryAfter = 0.1 / rate → very small

Wrong:

retryAfter = 1 / rate → too large ❌

---

👉 Using correct formula gives **precise retry timing**

---

## 🧠 7. Pseudocode

```
function check():

    acquire lock

    now = current time

    elapsedTime = now - lastRefillTime

    tokensToAdd = elapsedTime * refillRate

    tokens = min(capacity, tokens + tokensToAdd)

    lastRefillTime = now

    if tokens >= 1:
        tokens = tokens - 1
        allowed = true
        retryAfter = 0
    else:
        allowed = false
        tokensNeeded = 1 - tokens
        retryAfter = tokensNeeded / refillRate

    release lock

    return (allowed, retryAfter, remainingTokens)
```

---

## 🧠 8. Important Design Decisions

### 🔹 Lazy Refill

No background thread
Refill happens only on request

---

### 🔹 Thread Safety

Use lock / atomic operations

---

### 🔹 Precision

Use double for fractional tokens

---

## 🧠 9. Example Walkthrough

capacity = 5

refillRate = 1 token/sec

---

t = 0 → tokens = 5

5 requests → allowed → tokens = 0

6th request → rejected ❌

wait 1 sec → tokens = 1

next request → allowed ✅

---

## 🧠 10. Edge Cases

* Tokens must never exceed capacity
* Handle fractional tokens correctly
* Ensure thread safety
* Handle large time gaps safely
* Avoid precision loss

---

## 🎯 Final Mental Model

Token Bucket = Burst + Smooth Control

---

## 🚀 One-Line Summary

Token Bucket allows bursts but enforces a steady rate over time using continuously refilled tokens.

---
---

## 🧠 Phase 0: Algorithm Configuration Refactor (Enum Migration)

### 📌 Goal

Convert:

```yaml
algorithm: "token-bucket" ❌
```

into:

```yaml
algorithm: AlgorithmType.TOKEN_BUCKET ✅
```

---

### 🟢 Step 0.1 — Create Enum

Create an enum `AlgorithmType` in the core module under config package.

It should contain:

* `TOKEN_BUCKET`
* `FIXED_WINDOW`
* `LEAKY_BUCKET`

Ensure naming follows Java enum conventions and is uppercase.

---

### 🟢 Step 0.2 — Update Config Class

Update `RateLimiterConfig` to replace the algorithm field from `String` to `AlgorithmType`.

Make the following changes:

1. Replace the existing algorithm field with `AlgorithmType`
2. Update the builder pattern to accept `AlgorithmType`
3. Update getter to return `AlgorithmType`
4. Ensure immutability is preserved

---

### 🟢 Step 0.3 — Update Properties Mapping

Update `RateLimiterProperties` to support algorithm as a **String input** from `application.yml`.

* Do NOT change the YAML format
* Ensure this String is safely converted to `AlgorithmType` in the configuration layer

---

### 🟢 Step 0.4 — Update AutoConfiguration (MOST IMPORTANT)

Update `RateLimiterAutoConfiguration` to convert the String property `"algorithm"` into `AlgorithmType`.

Requirements:

1. Convert using:

```java
AlgorithmType.valueOf(properties.getAlgorithm().toUpperCase())
```

2. Provide default value:

```text
TOKEN_BUCKET
```

if property is null

3. Handle invalid values by throwing a clear `IllegalArgumentException`

4. Pass `AlgorithmType` to `RateLimiterConfig` builder

---

### 🟢 Step 0.5 — Update Factory

Update `RateLimiterFactory` to use `AlgorithmType` instead of String.

Requirements:

1. Replace all string comparisons with `switch-case` on `AlgorithmType`

2. Ensure mapping:

* `TOKEN_BUCKET` → `TokenBucketRateLimiter`
* `FIXED_WINDOW` → `FixedWindowRateLimiter`
* `LEAKY_BUCKET` → `LeakyBucketRateLimiter`

3. Throw `IllegalArgumentException` for unsupported values

---

### 🟢 Step 0.6 — Clean Up Old Code

Remove all String-based algorithm handling across the codebase.

Ensure:

1. No string comparisons like `equalsIgnoreCase` remain
2. No leftover `"strategy"` or `"algorithm"` string logic exists
3. All usages rely on `AlgorithmType` enum

---

### 🔍 Step 0.7 — Validation Prompt (Critical)

Use the following prompt with Copilot:

```text
Review the entire codebase and verify:

1. AlgorithmType enum is used consistently.
2. No string-based algorithm logic remains.
3. Factory correctly creates RateLimiter based on enum.
4. Application compiles without warnings or errors.
5. Default behavior works when algorithm is not specified.
```

---

## 🟢 Phase 1: Implementation Prompt

(Continue with Token Bucket / Fixed Window implementation steps below)

---

