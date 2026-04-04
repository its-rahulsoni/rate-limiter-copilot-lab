## 🟢 Part 1: Implementation Instructions (for Copilot)

### 📌 Objective

Implement the `SlidingWindowCounterRateLimiter` class based on the Sliding Window Counter algorithm.


### 🧩 Interface Contract

The class must implement:

```java
boolean allowRequest();
```

---

### ⚙️ Configuration

Use the following fields:

* `windowSizeInMillis` → size of window
* `maxRequests` → maximum allowed requests

---

### 🧠 Internal State

Maintain:

* `currentBucketCount` (int)
* `previousBucketCount` (int)
* `currentBucketStartTime` (long)

---

### ⏱ Time Handling

Use system time (or Clock for better testing):

```java
long currentTime = System.currentTimeMillis();
```

---

### 🔁 Core Logic

---

#### Step 1: Check bucket shift

```text
if currentTime >= currentBucketStartTime + windowSize:
    previousBucketCount = currentBucketCount
    currentBucketCount = 0
    currentBucketStartTime = currentTime
```

---

#### Step 2: Calculate overlap

```text
timeIntoBucket = currentTime - currentBucketStartTime
overlapRatio = (windowSize - timeIntoBucket) / windowSize
```

---

#### Step 3: Calculate effective count

```text
effectiveCount =
  currentBucketCount +
  (previousBucketCount × overlapRatio)
```

---

#### Step 4: Apply rate limiting

```text
if effectiveCount < maxRequests:
    currentBucketCount++
    allow
else:
    reject
```

---

### 🔒 Thread Safety

#### Recommended Approach

```java
public synchronized boolean allowRequest()
```

---

#### Advanced Option

* Use `ReentrantLock` for better scalability

---

### 📤 Behavior

* Allowed request → increment current bucket
* Rejected request → do NOT increment

---

### 🚫 Constraints

* Do NOT modify interfaces
* Do NOT use external libraries
* Ensure clean, production-ready code

---

## ⚠️ Edge Cases

1. **Boundary condition**

```text
timeIntoBucket = 0 or windowSize
```

2. **Bucket shift edge**

    * Multiple window jumps (large time gaps)

3. **High burst traffic**

    * Approximation may slightly miscount

4. **Time going backward**

    * Handle or document behavior

5. **windowSize = 0**

    * Throw exception or reject all

6. **Floating point precision**

    * overlapRatio must be double

---

## 🧪 Unit Tests Requirements

### Test Cases

1. Basic allow within limit
2. Reject when limit exceeded
3. Sliding behavior (overlap works correctly)
4. Bucket shift logic
5. Boundary conditions
6. Concurrent access
7. High throughput

---

## 🧵 Concurrency Testing

* Use `ExecutorService`
* Simulate parallel requests
* Verify correctness under load

---

## 🧹 Cleanup Strategy

* No cleanup required (O(1) memory)

---

## 🚀 Enhancements (Optional)

* Per-user rate limiting
* Distributed version (Redis)
* Metrics tracking

---

## 🟢 Part 2: Copilot Prompts

---

### 🔥 Step 1 — Implementation Prompt

```text
Read SlidingWindowCounterImpl.md and implement the SlidingWindowCounterRateLimiter class.

Follow all instructions strictly, including:
- configuration (windowSizeInMillis, maxRequests)
- maintaining currentBucketCount and previousBucketCount
- bucket shifting logic
- overlap calculation using double
- effectiveCount calculation
- synchronized method for thread safety

Do not modify interfaces.
Ensure clean, production-ready code.
```

---

### 🟡 Step 2 — Explanation Prompt

```text
Explain the SlidingWindowCounterRateLimiter implementation step by step and map each part of the code to SlidingWindowCounterAlgorithm.md.
```

---

### 🔍 Step 3 — Validation Prompt

```text
Review the SlidingWindowCounterRateLimiter implementation and verify:

1. Bucket shifting logic is correct.
2. overlapRatio calculation is correct.
3. effectiveCount formula is correctly implemented.
4. currentBucketCount updates correctly.
5. No race conditions exist.
6. Thread safety is ensured.
7. Code matches SlidingWindowCounterAlgorithm.md.

Highlight issues and suggest fixes.
```

---

### ⚠️ Step 4 — Edge Case Analysis

```text
Analyze the SlidingWindowCounterRateLimiter implementation for edge cases:

1. Bucket boundary transitions.
2. Large time jumps.
3. Floating point precision issues.
4. High burst traffic.
5. windowSize = 0.
6. Concurrent access issues.

Suggest improvements if needed.
```

---

### 🧪 Step 5 — Unit Test Prompt

```text
Generate unit tests for SlidingWindowCounterRateLimiter covering:

1. Normal request flow.
2. Rejection when limit exceeded.
3. Sliding window approximation correctness.
4. Bucket shift scenarios.
5. Boundary conditions.
6. Concurrent access.
7. Stress testing.

Use JUnit and Mockito.
```

---

### 🚀 Step 6 — Performance Review Prompt

```text
Analyze the performance of SlidingWindowCounterRateLimiter:

- Time complexity per request
- Memory usage
- Behavior under high load

Suggest optimizations if needed.
```

---

## 🧠 Part 3: Manual Sanity Checklist

* Bucket shift:

```text
currentTime >= currentBucketStartTime + windowSize
```

* Overlap:

```text
overlapRatio = (windowSize - timeIntoBucket) / windowSize
```

* Effective count:

```text
current + (previous × overlapRatio)
```

* Allow condition:

```text
effectiveCount < maxRequests
```

---

## 🚨 Common Bugs to Watch

* Incorrect bucket shifting
* Wrong overlap calculation
* Using integer instead of double
* Race conditions
* Updating bucket on reject
* Not handling large time gaps

---

## 🔁 Recommended Workflow

1. Review `SlidingWindowCounterAlgorithm.md`
2. Generate implementation using Copilot
3. Ask for explanation
4. Validate using prompts
5. Check edge cases
6. Add unit tests

---

## 🎯 Key Insight

Sliding Window Counter provides:

* Near accuracy
* Constant memory
* High scalability

It is widely used in **production systems** where:

* performance matters more than perfect accuracy 🚀
