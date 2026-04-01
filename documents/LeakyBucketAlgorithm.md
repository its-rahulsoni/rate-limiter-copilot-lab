# Leaky Bucket Rate Limiting Algorithm

---

## 🧠 1. Core Idea (Intuition First)

👉 Leaky Bucket enforces a **constant outflow rate**, regardless of incoming burst.

---

### 🔥 Think like this:

A bucket with a hole at the bottom.

* Water (requests) comes in
* Water leaks out at a **fixed rate**

---

### Example:

capacity = 5
leakRate = 1 request/sec

---

👉 Meaning:

Requests are processed at a steady rate (1/sec), even if they arrive in bursts.

---

## 🧠 2. Key Behavior

---

### Incoming requests:

* Can arrive in bursts

---

### Outgoing processing:

* Always smooth and constant

---

👉 Key property:

Leaky Bucket = **Smooth Output**

---

## 🧠 3. Two Ways to Think About It

---

### 🟢 Queue Model (Conceptual)

Requests are queued and processed at a fixed rate.

---

### 🟡 Counter Model (Implementation)

Track how full the bucket is and reduce it over time.

---

👉 We implement the **counter model** (more efficient).

---

## 🧠 4. Core Variables

* capacity → max bucket size
* currentWater → current level (requests in system)
* leakRate → requests processed per second
* lastLeakTime → last time leakage was calculated

---

## 🧠 5. Core Logic (With Reasoning)

---

### Step 1: Calculate leakage

```text id="lba1"
elapsedTime = now - lastLeakTime
leaked = elapsedTime × leakRate
currentWater = max(0, currentWater - leaked)
```

---

### 🧠 Why are we multiplying?

We compute:

```text
leaked = elapsedTime × leakRate
```

---

### Units Explanation:

* elapsedTime → seconds
* leakRate → requests per second

---

So:

```text
seconds × (requests/sec) = requests
```

---

👉 We are calculating:

> **How many requests have been processed (leaked) during the elapsed time**

---

### 🔥 Example 1

```text
leakRate = 2 req/sec
elapsedTime = 3 sec
```

---

```text
leaked = 3 × 2 = 6 requests
```

👉 Meaning:

6 requests have been processed in 3 seconds.

---

### 🔥 Example 2 (Fractional Case)

```text
leakRate = 2 req/sec
elapsedTime = 0.5 sec
```

---

```text
leaked = 0.5 × 2 = 1 request
```

👉 Even in half a second, 1 request is processed.

---

### ❗ Why NOT use step-based logic?

Wrong approach:

```text
if (1 second passed) → leak requests
```

---

### Problem:

* 0.9 sec → no leak ❌
* 1 sec → sudden jump ✔

---

👉 This creates uneven behavior.

---

### ✅ Correct approach:

Continuous leakage:

```text
leaked = elapsedTime × leakRate
```

---

### 🧠 What we are simulating:

```text
Water draining continuously from the bucket
```

---

### Step 2: Add incoming request

```text id="lba2"
if currentWater + 1 <= capacity:
    currentWater++
    allow
else:
    reject
```

---

### 🧠 Why this logic?

We are checking:

```text
Is there space in the bucket?
```

---

### Example:

```text
capacity = 5
currentWater = 4
```

---

Incoming request:

```text
4 + 1 = 5 → allowed
```

---

Next request:

```text
5 + 1 = 6 → exceeds capacity → reject
```

---

👉 This ensures:

```text
Bucket never overflows
```

---

### 🧠 Interpretation

* `currentWater` = number of requests waiting/processing
* `capacity` = system limit

---

👉 So:

```text
currentWater + 1 <= capacity
```

means:

> “Can the system accept one more request?”

---

## 🧠 6. retryAfter Concept

---

If request is rejected:

```text id="lba3"
excessWater = currentWater - capacity + 1
retryAfter = excessWater / leakRate
```

---

### 🧠 Why this formula?

We need to calculate:

> “How long until enough requests are processed to make space?”

---

### Example:

```text
capacity = 5
currentWater = 6
leakRate = 1 req/sec
```

---

```text
excessWater = 6 - 5 = 1
retryAfter = 1 / 1 = 1 sec
```

---

👉 After 1 second → space available

---

### Units:

```text
requests / (requests/sec) = seconds
```

---

## 🧠 7. Pseudocode

```text id="lba4"
function check():

    acquire lock

    now = current time

    elapsedTime = now - lastLeakTime

    leaked = elapsedTime * leakRate

    currentWater = max(0, currentWater - leaked)

    lastLeakTime = now

    if currentWater + 1 <= capacity:
        currentWater += 1
        allowed = true
        retryAfter = 0
    else:
        allowed = false
        excessWater = currentWater - capacity + 1
        retryAfter = excessWater / leakRate

    release lock

    return (allowed, retryAfter, currentWater, capacity)
```

---

## 🧠 8. Example Walkthrough

---

capacity = 5
leakRate = 1 req/sec

---

t = 0 → 5 requests → bucket full

t = 1 → 1 leaked → space = 1

t = 2 → another leaked → space = 2

---

👉 Extra requests during full capacity → rejected

---

## 🧠 9. Key Difference from Token Bucket

| Feature       | Token Bucket | Leaky Bucket    |
| ------------- | ------------ | --------------- |
| Burst allowed | ✅ Yes        | ❌ No            |
| Output rate   | Variable     | Constant        |
| Behavior      | Flexible     | Strict          |
| Use case      | APIs         | Traffic shaping |

---

## 🧠 10. Real-World Analogy

---

### Token Bucket:

You can spend saved tokens → burst allowed

---

### Leaky Bucket:

You must stand in a queue → fixed speed

---

## 🧠 11. When to Use Leaky Bucket

---

### Use when:

✔ strict rate control required
✔ smooth traffic needed
✔ downstream systems are sensitive

---

### Avoid when:

❌ burst handling is needed
❌ user experience requires flexibility

---

## 🧠 12. Edge Cases

---

* currentWater must not go below 0
* currentWater must not exceed capacity
* leakRate = 0 → division by zero
* large idle time → bucket should empty completely

---

## 🎯 Final Mental Model

```text id="lba5"
Leaky Bucket = Queue + Constant Drain Rate
```

---

## 🚀 One-Line Summary

Leaky Bucket ensures a steady request processing rate by draining requests at a fixed speed and rejecting excess bursts.

---
