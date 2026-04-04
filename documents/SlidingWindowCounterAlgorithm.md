Got it 👍 — the issue was that I wrapped the **entire document inside a ```markdown block**, which breaks heading rendering and causes inconsistent sizes.

Now I’m fixing it properly:

* ❌ No outer markdown code block
* ✅ Proper headings (##, ###)
* ✅ Only **code / pseudo / formulas** inside blocks
* ✅ Same structure as your Leaky Bucket file

---

# SlidingWindowCounterAlgorithm.md

## 🔹 1. Core Intuition

The Sliding Window Counter algorithm provides:

> Near-accurate rate limiting with constant memory and high performance

Instead of storing every request (like Sliding Window Log), it:

* Groups requests into time buckets
* Uses mathematical approximation to estimate requests in the current window

---

## ⚙️ 2. Basic Idea

We maintain only:

* currentBucketCount
* previousBucketCount
* currentBucketStartTime

---

## ⏱️ 3. Time Division

Assume:

* Window size = 60 seconds

We divide time into buckets of the same size:

```text
Bucket 1 → [0 - 60)
Bucket 2 → [60 - 120)
Bucket 3 → [120 - 180)
```

---

## 📊 4. Problem with Fixed Buckets

Using only fixed buckets leads to burst issues:

Example:

* 100 requests at t = 59
* 100 requests at t = 60

→ Total = 200 requests in ~1 second ❌

---

## 💡 5. Sliding Approximation

To fix this, we combine:

* Current bucket → exact count
* Previous bucket → partial contribution

---

## 🧮 6. Core Formula

```text
effective_count =
  current_bucket_count +
  (previous_bucket_count × overlap_ratio)
```

---

## 🧠 7. Understanding overlap_ratio

```text
overlap_ratio =
  (time overlapping with previous bucket) / window_size
```

---

## 📌 8. Example (Step-by-Step)

### Given:

* Window = 60 sec
* Current time = 75 sec

Window:

```text
[15 → 75]
```

Buckets:

* Previous → [0–60]
* Current → [60–120]

---

### Overlap with previous bucket:

```text
[15 → 60] = 45 sec
overlap_ratio = 45 / 60 = 0.75
```

---

### Suppose:

* previousBucketCount = 100
* currentBucketCount = 20

```text
effective_count = 20 + (100 × 0.75)
                = 95
```

---

## 🤯 Key Assumption

We assume:

> Requests are uniformly distributed within a bucket

---

## ⚠️ Limitation

* Not perfectly accurate (bursty traffic)
* But:

    * Very efficient
    * Good enough for production systems

---

## 🔁 9. Algorithm Flow

At every request:

### Step 1: Get current time

---

### Step 2: Check if we moved to a new bucket

If yes:

* previousBucketCount = currentBucketCount
* currentBucketCount = 0
* currentBucketStartTime = currentTime

---

### Step 3: Calculate overlap

```text
timeIntoBucket = currentTime - currentBucketStartTime
overlapRatio = (windowSize - timeIntoBucket) / windowSize
```

---

### Step 4: Compute effective count

```text
effectiveCount =
  currentBucketCount +
  (previousBucketCount × overlapRatio)
```

---

### Step 5: Apply rate limiting

* If effectiveCount < maxRequests → allow
* Else → reject

---

## ⚙️ 10. Pseudocode

```pseudo
class SlidingWindowCounterRateLimiter:

    windowSize
    maxRequests

    currentBucketCount = 0
    previousBucketCount = 0
    currentBucketStartTime

    function allowRequest(currentTime):

        # Step 1: Check bucket shift
        if currentTime >= currentBucketStartTime + windowSize:

            previousBucketCount = currentBucketCount
            currentBucketCount = 0
            currentBucketStartTime = currentTime

        # Step 2: Calculate overlap
        timeIntoBucket = currentTime - currentBucketStartTime
        overlapRatio = (windowSize - timeIntoBucket) / windowSize

        # Step 3: Effective count
        effectiveCount =
            currentBucketCount +
            (previousBucketCount * overlapRatio)

        # Step 4: Apply limit
        if effectiveCount < maxRequests:
            currentBucketCount += 1
            return true
        else:
            return false
```

---

## ⚡ 11. Complexity

* Time Complexity:

    * O(1) per request

* Space Complexity:

    * O(1)

---

## 🔥 12. Key Observations

* No request logs → very memory efficient
* Uses approximation → slight inaccuracy possible
* Smooths boundary bursts

---

## 🧠 Final Mental Model

Sliding Window Counter:

> Estimate request count using bucket math instead of storing all requests

---

## ✅ Summary

* Uses only 2 buckets
* Combines:

    * exact (current bucket)
    * weighted (previous bucket)
* Highly scalable and production-friendly

---

Now this matches your **Leaky Bucket formatting exactly** ✅

Next step (recommended):
👉 I can create **SlidingWindowCounterImpl.md (Copilot instructions + prompts)** in the same format.
