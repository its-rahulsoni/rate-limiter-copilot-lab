## `SlidingWindowLogAlgorithm`

```markdown
# Sliding Window Log Algorithm (Rate Limiting)

## 🔹 1. Core Intuition

The Sliding Window Log algorithm answers:

> “How many requests happened in the last `T` seconds from *right now*?”

Instead of grouping time into buckets, it:
- Stores **exact timestamps of every request**
- Continuously **slides the window with time**

---

## ⏱️ 2. How It Works (Conceptually)

Assume:
- Limit = **5 requests**
- Window = **60 seconds**

We maintain:

```

request_log = [timestamps of requests]

```

### When a new request comes:

1. Remove timestamps **older than (current_time - window_size)**
2. Check size of remaining timestamps
3. If `< limit` → allow request
4. Else → reject

---

## 📌 3. Example Walkthrough

### Scenario:
- Window = 60 sec
- Limit = 3 requests

#### Requests come at:
```

t = 10 → allow → log = [10]
t = 20 → allow → log = [10, 20]
t = 30 → allow → log = [10, 20, 30]
t = 50 → reject → log still = [10, 20, 30]

```

---

## 🔹 Understanding Time (Important Clarification)

When we say:
```

t = 10, 20, 30, 75

```

This does **NOT** mean:
- seconds inside a minute (0–59)

Instead, it means:
- **continuous time (like epoch time or system time in seconds)**

### Think of time like this:
```

Time → 0 → 10 → 20 → 30 → ... → 60 → 75 → 120 → ...

```

👉 Time does **not reset after 60**

---

## 🔹 What Does `window = 60 sec` Mean?

It means:

> “At any moment, look back the last 60 seconds from NOW”

So at any time:
```

window = [current_time - window_size → current_time]

```

---

## 🧾 4. Data Structures

Best choice:
- **Queue / Deque**

### Why?
- Oldest request is always at the front → easy removal
- New requests appended at the end

---

## ⚙️ 5. Pseudocode

```

class SlidingWindowLogRateLimiter:
window_size
max_requests
queue = empty deque

```
function allow_request(current_time):

    # Step 1: Remove expired requests
    while queue is not empty AND queue.front <= current_time - window_size:
        queue.pop_front()

    # Step 2: Check limit
    if queue.size < max_requests:
        queue.push_back(current_time)
        return true
    else:
        return false
```

```

---

## ⚡ 6. Complexity

### Time Complexity:
- Amortized **O(1)** per request

### Space Complexity:
- **O(N)** where N = number of requests in the window

---

## ⚠️ 7. Key Observations

- Window is **not fixed**
- It moves **per request**
- This is why it's called *sliding*

---

## 🧠 Final Mental Model

Sliding Window Log is essentially asking:

> “What exactly happened in the last T seconds?”

- Exact timestamps → **perfect accuracy**
- No approximation
- Simple logic, but higher memory usage

---
```
