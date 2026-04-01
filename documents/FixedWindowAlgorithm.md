# Fixed Window Rate Limiting Algorithm

---

## 🧠 1. Core Idea (Intuition First)

👉 Fixed Window limits requests in **fixed time intervals**

---

### 🔥 Think like this:

“You can make at most **N requests in every X seconds**”

---

### Example:

limit = 5 requests

window = 10 seconds

---

👉 Meaning:

In each 10-second window → max 5 requests allowed

---

## 🧠 2. How It Works

---

## Divide time into windows:

0–10 sec   → Window 1

10–20 sec  → Window 2

20–30 sec  → Window 3

---

## Each window maintains:

* requestCount

---

## Request Flow:

* if requestCount < limit → allow ✅
* else → reject ❌

---

## 🧠 3. Core Variables

* limit → max requests per window
* windowSize → duration (in milliseconds or seconds)
* currentWindow → current window identifier
* requestCount → number of requests in current window

---

## 🧠 4. How to Identify Window

---

### Formula:

windowId = currentTime / windowSize

---

### Example:

currentTime = 25 sec

windowSize = 10 sec

---

windowId = 25 / 10 = 2

---

👉 So request belongs to:

Window 2 (20–30 sec)

---

## 🧠 5. Core Logic (Step-by-Step)

---

### Step 1: Get current window

windowId = now / windowSize

---

### Step 2: Check if window changed

If windowId != currentWindow:

* currentWindow = windowId
* requestCount = 0

---

### Step 3: Apply limit

If requestCount < limit:

* requestCount++
* allow

Else:

* reject

---

## 🧠 6. retryAfter Concept

---

When request is rejected:

👉 We calculate:

“How long until the next window starts?”

---

### Formula:

windowEnd = (currentWindow + 1) × windowSize

retryAfter = windowEnd - currentTime

---

### Example:

windowSize = 10 sec

currentTime = 27 sec

currentWindow = 2

---

windowEnd = (2 + 1) × 10 = 30

retryAfter = 30 - 27 = 3 seconds

---

👉 After 3 seconds → new window starts → requests allowed again ✅

---

## 🧠 7. Pseudocode

```id="fwcode1"
function check():

    now = current time

    windowId = now / windowSize

    if windowId != currentWindow:
        currentWindow = windowId
        requestCount = 0

    if requestCount < limit:
        requestCount++
        allowed = true
        retryAfter = 0
    else:
        allowed = false
        windowEnd = (currentWindow + 1) * windowSize
        retryAfter = windowEnd - now

    return (allowed, retryAfter)
```

---

## 🧠 8. Example Walkthrough

---

### Config:

limit = 5

window = 10 sec

---

### Timeline:

t = 1 → request 1 → allowed

t = 2 → request 2 → allowed

t = 3 → request 3 → allowed

t = 4 → request 4 → allowed

t = 5 → request 5 → allowed

t = 6 → request 6 → rejected ❌

---

### New Window:

t = 10 → counter resets

---

👉 Requests allowed again ✅

---

## 🧠 9. Major Problem (VERY IMPORTANT)

---

### ❗ Boundary Burst Problem

---

### Scenario:

limit = 5

window = 10 sec

---

### Timeline:

t = 9.9  → 5 requests

t = 10.1 → 5 requests

---

👉 Total:

10 requests in ~0.2 seconds 😱

---

### ❗ Why This Happens

Window resets abruptly

---

👉 This leads to **unfair burst behavior**

---

## 🧠 10. Comparison with Token Bucket

---

| Feature        | Fixed Window  | Token Bucket |
| -------------- | ------------- | ------------ |
| Burst handling | ❌ Poor        | ✅ Good       |
| Smooth rate    | ❌ No          | ✅ Yes        |
| Simplicity     | ✅ Very simple | ⚠️ Medium    |
| Accuracy       | ❌ Lower       | ✅ High       |

---

## 🧠 11. When to Use Fixed Window

---

### Use when:

✔ simple systems

✔ low traffic

✔ approximate control is acceptable

---

### Avoid when:

❌ high traffic APIs

❌ fairness is important

❌ precise rate limiting required

---

## 🎯 Final Mental Model

Fixed Window = Count requests in fixed time bucket

---

## 🚀 One-Line Summary

Fixed Window allows a fixed number of requests per time interval but suffers from burst issues at window boundaries.

---
