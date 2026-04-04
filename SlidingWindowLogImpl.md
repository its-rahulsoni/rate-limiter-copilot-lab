
---

# Sliding Window Log – Implementation Guide & Copilot Workflow

This section defines how to implement the Sliding Window Log rate limiting algorithm using GitHub Copilot.

---

## 🟢 Part 1: Implementation Instructions (for Copilot)

### 📌 Objective

Implement the `SlidingWindowLogRateLimiter` class based on the Sliding Window Log algorithm.

---

### 🧩 Interface Contract

The class must expose:

```java id="swl1"
boolean allowRequest();
```

---

### ⚙️ Configuration

Use the following fields:

* `windowSizeInMillis` → size of sliding window
* `maxRequests` → maximum allowed requests in window

---

### 🧠 Internal State

Maintain:

* `Deque<Long> requestLog` → stores timestamps of requests

---

### ⏱ Time Handling

* Use system time (or Clock for testability)

```java id="swl2"
long currentTime = System.currentTimeMillis();
```

---

### 🔁 Core Logic

---

#### Step 1: Calculate window start

```text id="swl3"
windowStart = currentTime - windowSizeInMillis
```

---

#### Step 2: Remove expired requests

```text id="swl4"
while requestLog not empty AND requestLog.peekFirst() <= windowStart:
    remove from front
```

👉 Important:

* `<=` is required (boundary condition)

---

#### Step 3: Apply rate limiting

```text id="swl5"
if requestLog.size < maxRequests:
    add currentTime to log
    allow
else:
    reject
```

---

### 🔒 Thread Safety

#### Recommended Approach

* Use synchronized method:

```java id="swl6"
public synchronized boolean allowRequest()
```

---

#### Alternative (Advanced)

* Use `ReentrantLock` for finer control

---

### 📤 Behavior

* Allow request → store timestamp
* Reject request → do NOT store timestamp

---

### 🚫 Constraints

* Do NOT modify interfaces
* Do NOT use external libraries
* Ensure clean, production-ready code

---

## ⚠️ Edge Cases to Handle

1. **Burst traffic**

    * Multiple requests at same millisecond

2. **Boundary condition**

   ```text id="swl7"
   timestamp == windowStart → MUST be removed
   ```

3. **Empty log**
   - First request should always be allowed

4. **Large traffic**
   - Ensure expired entries are removed (avoid memory leak)

5. **Time going backward**
   - Document behavior (ignore or reject)

6. **windowSize = 0**
   - Throw exception or reject all requests

---

## 🧪 Unit Tests Requirements

### Test Cases

1. **Basic Allow**
   - Within limit → all allowed

2. **Limit Exceeded**
   - Extra request → rejected

3. **Sliding Behavior**
   - Old requests expire correctly

4. **Boundary Case**
   - Exact edge timestamp handling

5. **Concurrent Access**
   - Multiple threads calling allowRequest()

6. **High Throughput**
   - Stress scenario

---

## 🧵 Concurrency Testing Strategy

* Use `ExecutorService`
* Simulate parallel requests
* Verify:
  - No more than maxRequests allowed
  - No race conditions

---

## 🧹 Cleanup Strategy

* Lazy cleanup (on each request)
* No background thread required

---

## 🚀 Enhancements (Optional)

* Per-user rate limiting:
  - `Map<UserId, Deque<Long>>`

* Distributed version:
  - Redis-based implementation

* Metrics:
  - allowed vs rejected count

---

## 🟢 Part 2: Copilot Prompts

---

### 🔥 Step 1 — Implementation Prompt

```text id="swlp1"
Read SlidingWindowLogAlgorithm.md and implement the SlidingWindowLogRateLimiter class.

Follow all instructions strictly, including:
- configuration (windowSizeInMillis, maxRequests)
- use of Deque<Long> for storing timestamps
- removal of expired requests using (currentTime - windowSize)
- boundary condition (<= windowStart)
- synchronized method for thread safety

Ensure clean, production-ready code.
Do not modify interfaces.
````

---

### 🟡 Step 2 — Explanation Prompt

```text id="swlp2"
Explain the SlidingWindowLogRateLimiter implementation step by step and map each part of the code to SlidingWindowLogAlgorithm.md.
```

---

### 🔍 Step 3 — Validation Prompt

```text id="swlp3"
Review the SlidingWindowLogRateLimiter implementation and verify:

1. Expired requests are removed correctly.
2. Boundary condition (<= windowStart) is handled.
3. requestLog size never exceeds maxRequests.
4. No expired entries remain in the queue.
5. Thread safety is ensured (synchronized or locking).
6. No race conditions exist.
7. Code matches SlidingWindowLogAlgorithm.md.

Highlight issues and suggest fixes.
```

---

### ⚠️ Step 4 — Edge Case Analysis

```text id="swlp4"
Analyze the SlidingWindowLogRateLimiter implementation for edge cases:

1. Multiple requests at same timestamp.
2. Boundary condition at window edge.
3. Empty request log.
4. Very large number of requests.
5. Time going backward.
6. windowSize = 0.
7. High concurrency scenarios.

Suggest improvements if needed.
```

---

### 🧪 Step 5 — Unit Test Prompt

```text id="swlp5"
Generate unit tests for SlidingWindowLogRateLimiter covering:

1. Normal request flow.
2. Rejection when limit exceeded.
3. Expiration of old requests.
4. Boundary condition handling.
5. Concurrent access scenarios.
6. Stress testing.

Use JUnit and Mockito.
```

---

## 🧠 Part 3: Manual Sanity Checklist

* Window calculation:

```text id="swl8"
windowStart = currentTime - windowSize
```

* Removal logic:

```text id="swl9"
remove timestamps <= windowStart
```

* Allow condition:

```text id="swl10"
requestLog.size < maxRequests
```

* Data structure:

    * FIFO order maintained

---

## 🚨 Common Bugs to Watch

* Not removing expired entries → memory leak
* Incorrect boundary condition (`<` instead of `<=`)
* Missing synchronization → race conditions
* Adding rejected requests to log
* Incorrect time calculation

---

## 🔁 Recommended Workflow

1. Review `SlidingWindowLogAlgorithm.md`
2. Generate implementation using Copilot
3. Ask for explanation
4. Validate using prompts
5. Check edge cases
6. Add unit tests

---

## 🎯 Key Insight

Sliding Window Log provides:

* **Perfect accuracy**
* Simple logic
* Higher memory usage

It is best suited for:

* low to medium traffic systems
* correctness-critical use cases

---
