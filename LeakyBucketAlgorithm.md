# Leaky Bucket – Implementation Guide & Copilot Workflow

This section defines how to implement the Leaky Bucket rate limiting algorithm using GitHub Copilot.

---

## 🟢 Part 1: Implementation Instructions (for Copilot)

### 📌 Objective

Implement the `LeakyBucketRateLimiter` class based on the Leaky Bucket algorithm.

---

### 🧩 Interface Contract

The class must implement:

```java id="lbif1"
boolean allow();
RateLimitResult check();
```

---

### ⚙️ Configuration

Use the following fields from `RateLimiterConfig`:

* `capacity` → maximum bucket size
* `leakRate` → requests processed per second

---

### 🧠 Internal State

Maintain:

* `currentWater` (double)
* `lastLeakTime` (Instant)

---

### ⏱ Time Handling

* Use `java.time.Clock`
* Retrieve time using:

```java id="lbif2"
Instant now = clock.instant();
```

---

### 🔁 Core Logic

---

#### Step 1: Calculate leakage

```text id="lbif3"
elapsedTime = now - lastLeakTime
leaked = elapsedTime × leakRate
currentWater = max(0, currentWater - leaked)
```

---

#### Step 2: Update last leak time

```text id="lbif4"
lastLeakTime = now
```

---

#### Step 3: Apply rate limiting

```text id="lbif5"
if currentWater + 1 <= capacity:
    currentWater++
    allow
else:
    reject
```

---

### ⏳ retryAfter Calculation

When request is rejected:

```text id="lbif6"
excessWater = currentWater - capacity + 1
retryAfter = excessWater / leakRate
```

---

### 🔒 Thread Safety

* Use `ReentrantLock`
* Ensure all state updates are thread-safe

---

### 📤 Response

Return `RateLimitResult`:

* allowed
* retryAfter
* remainingCapacity = capacity - currentWater
* capacity

---

### 🚫 Constraints

* Do NOT modify interfaces
* Do NOT use external libraries
* Ensure clean, readable, production-quality code

---

## 🟢 Part 2: Copilot Prompts

---

### 🔥 Step 1 — Implementation Prompt

```text id="lbp1"
Read LeakyBucketAlgorithm.md and implement the LeakyBucketRateLimiter class.

Follow all instructions strictly, including:
- interface contract (RateLimiter)
- configuration usage (capacity, leakRate)
- state variables (currentWater, lastLeakTime)
- time handling using Clock
- leakage calculation (elapsedTime × leakRate)
- retryAfter calculation
- thread safety using ReentrantLock

Do not change any existing interfaces or configs.
Ensure clean, production-ready code.
```

---

### 🟡 Step 2 — Explanation Prompt

```text id="lbp2"
Explain the LeakyBucketRateLimiter implementation step by step and map each part of the code to LeakyBucketAlgorithm.md.
```

---

### 🔍 Step 3 — Validation Prompt

```text id="lbp3"
Review the LeakyBucketRateLimiter implementation and verify:

1. Leakage calculation is correct (elapsedTime × leakRate).
2. currentWater never goes below 0.
3. currentWater never exceeds capacity.
4. lastLeakTime is updated correctly.
5. retryAfter calculation is accurate.
6. Thread safety is ensured using ReentrantLock.
7. No race conditions exist.
8. Code aligns with LeakyBucketAlgorithm.md.

Highlight issues and suggest fixes.
```

---

### ⚠️ Step 4 — Edge Case Analysis

```text id="lbp4"
Analyze the LeakyBucketRateLimiter implementation for edge cases:

1. currentWater close to capacity boundary.
2. currentWater = 0 scenario.
3. leakRate = 0 (division by zero).
4. large idle periods (bucket should empty).
5. high concurrency scenarios.
6. floating point precision issues.
7. rapid burst requests.

Suggest improvements if needed.
```

---

### 🧪 Step 5 — Unit Test Prompt

```text id="lbp5"
Generate unit tests for LeakyBucketRateLimiter covering:

1. Normal request flow.
2. Rejection when capacity exceeded.
3. Leakage over time.
4. retryAfter correctness.
5. Edge cases (zero water, near capacity).
6. Concurrent access scenarios.

Use JUnit and Mockito.
```

---

## 🧠 Part 3: Manual Sanity Checklist

* Leakage logic:

    * `leaked = elapsedTime × leakRate`

* Water bounds:

    * `0 ≤ currentWater ≤ capacity`

* Time update:

    * `lastLeakTime = now`

* Allow logic:

    * `currentWater + 1 ≤ capacity`

* retryAfter:

    * `(excessWater / leakRate)`

---

## 🚨 Common Bugs to Watch

* Negative water values
* Overflow beyond capacity
* Incorrect retryAfter
* Division by zero (leakRate = 0)
* Missing locking → race conditions
* Incorrect time calculations

---

## 🔁 Recommended Workflow

1. Review `LeakyBucketAlgorithm.md`
2. Generate implementation using Copilot
3. Ask for explanation
4. Validate using prompts
5. Check edge cases
6. Add unit tests

---

## 🎯 Key Insight

Leaky Bucket enforces **strict, smooth rate limiting** and must be implemented with:

* precise time calculations
* correct leakage logic
* strong thread safety

---
