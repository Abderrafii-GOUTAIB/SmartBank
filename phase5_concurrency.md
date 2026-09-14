# ⚡ Phase 5 — The Concurrent Bank
## Complete Reference: Java Concurrency

> SmartBank — Java Backend Learning Path
> Use this as your interview cheat sheet for threading & concurrency.

---

# THE BIG PICTURE

```
Phase 5 is the HARDEST phase in pure Java.
Concurrency bugs are INVISIBLE and RANDOM —
code runs fine 999 times, corrupts data on run 1000.

Your console app barely needs concurrency.
You learn it for your CAREER — every backend server
(Stage 3, Spring Boot) is multi-threaded.
```

---

# Step 46 — Thread Basics (Runnable)

**Definition:** A thread is an independent path of execution. Multiple threads run concurrently within one program.

```
SINGLE THREAD — one teller, everyone queues:
  Customer1 → Customer2 → Customer3 (wait their turn)

MANY THREADS — many tellers, parallel:
  Customer1 → Teller1
  Customer2 → Teller2   all at the same time
  Customer3 → Teller3
```

**Key distinction:**
```
task.run()              → normal method call, SAME thread, NO concurrency
new Thread(task).start()→ NEW thread, run() executes there, concurrency ✅
```

- `Runnable` is a `@FunctionalInterface` (one method: `run()`) → pass a lambda
- `start()` → launch thread, don't wait
- `join()` → wait for that thread to finish

---

# Step 47 — Synchronization & Race Conditions

**Race condition:** Two+ threads access shared data at the same time, and the result depends on the unpredictable ORDER of execution.

```
THE ATM DISASTER — balance $1000, two withdraw $1000:

  Thread A: read balance → $1000
  Thread B: read balance → $1000   ← both read same value
  Thread A: check 1000>=1000 ✅ → balance = 0
  Thread B: check 1000>=1000 ✅ → balance = -1000
  → $2000 dispensed from $1000 account 💥
```

**synchronized:** puts a LOCK on a critical section — one thread at a time.

```
synchronized = one-lane bridge: cars cross one at a time, never crash.

ACC-0001 has its own lock 🔒
ACC-0002 has its own lock 🔒

Two threads, SAME account  → one waits ✅
Two threads, DIFFERENT accounts → both run in parallel ✅
```

**SmartBank:** `deposit()`, `withdraw()`, `getBalance()` are `synchronized`.

---

# Step 48 — volatile & AtomicInteger

```
volatile      → VISIBILITY: everyone reads/writes main memory,
                no stale cached copies. For simple flags.

AtomicInteger → ATOMICITY: read+add+write as ONE unbreakable
                step. For counters (++).

synchronized  → VISIBILITY + ATOMICITY for multi-step logic.
```

**Why volatile alone fails for `count++`:**
```
count++ is 3 steps: read, add, write.
volatile makes each step hit main memory,
but another thread still sneaks BETWEEN steps.
→ need AtomicInteger for atomic ++.
```

**The decision guide:**
```
Simple flag (true/false), one writer?  → volatile
Counter many threads increment?        → AtomicInteger
Multi-step "check then act"?            → synchronized
```

**volatile vs AtomicBoolean:**
```
volatile boolean → visibility only (write it, read it)
AtomicBoolean    → visibility + compareAndSet
                   ("if false, set true" atomically — for "start once")
```

**SmartBank:** `nextId` → AtomicInteger (unique IDs); `running` flag → volatile.

---

# Step 49 — ExecutorService & Thread Pools

**Definition:** A manager that runs tasks on a POOL of reusable threads — handles creation, reuse, queuing, shutdown.

```
WITHOUT pool: hire a worker per task, fire after → expensive,
              10,000 tasks = 10,000 threads = crash 💥

WITH pool: hire 4 workers, REUSE them for all tasks.
           tasks queue (FIFO), workers grab the next.
```

```
3 lines you need:
  ExecutorService pool = Executors.newFixedThreadPool(4);
  pool.submit(() -> doWork());
  pool.shutdown();   // CRITICAL — or app never exits
```

**Runnable vs Callable:**
```
Runnable → run()  → returns nothing
Callable → call() → returns a value
Future   → a promise for a result coming later (future.get())
```

**Pool types:**
```
newFixedThreadPool(n)     → n reusable threads
newSingleThreadExecutor() → 1 thread, ordered tasks
newCachedThreadPool()     → grows/shrinks as needed
```

---

# Step 50 — CompletableFuture

**Definition:** A Future you can build a PIPELINE on — run work async, then chain "when done, do this next" without blocking.

```
Future.get()       → BLOCKS you until result is ready
CompletableFuture  → describe the pipeline, walk away, runs automatically
```

```
CompletableFuture
    .supplyAsync(() -> calculateTotal())   // run async
    .thenApply(total -> format(total))      // then transform
    .thenAccept(text -> print(text))        // then use
    .exceptionally(err -> handle(err));     // handle errors
```

**Key insight:** frees the CALLER (no waiting), but the WORKER stays busy until done. Real speedup comes from running multiple workers in PARALLEL (`thenCombine`), not from making one task faster.

**Method map:**
```
supplyAsync  → start async work returning a value
thenApply    → transform result → new result
thenAccept   → consume result, return nothing
thenCombine  → wait for two, merge results
exceptionally→ handle errors in the chain
```

**Use:** Stage 3+ calling slow external services (fraud API, credit score) in parallel.

---

# Step 51 — Concurrent Collections

**Definition:** Thread-safe collection versions with fine-grained locking — safe AND fast, no manual `synchronized` blocks.

```
Why plain HashMap is dangerous:
  Two threads writing at once corrupt the map's INTERNAL
  arrays/pointers during resize → infinite loop / lost data.
  This is INSIDE the map — synchronized on YOUR code can't reach it.

synchronized   → protects YOUR multi-step logic
ConcurrentHashMap → protects the MAP'S OWN internals
  (two different problems!)
```

```
HashMap   → ConcurrentHashMap          (per-bucket locking, fast)
HashSet   → ConcurrentHashMap.newKeySet()
ArrayList → CopyOnWriteArrayList       (read-heavy, write-rarely)
```

**Read-heavy vs write-heavy:**
```
CopyOnWriteArrayList copies the whole array on every WRITE.
  → great for read-often/write-rarely (accounts list ✅)
  → terrible for write-thousands-per-second (too much copying)

Match the collection to your read/write pattern.
```

**SmartBank:** `AccountRepository` — accounts → CopyOnWriteArrayList, maps/sets → ConcurrentHashMap.

---

# Step 52 — Deadlock: Prevention

**Deadlock:** Two+ threads each holding a lock the other needs, each waiting forever. No crash — just a frozen hang.

```
Thread 1: locks ACC-001, needs ACC-002
Thread 2: locks ACC-002, needs ACC-001
→ circular wait → frozen forever 💀
```

**The 4 conditions (ALL needed for deadlock):**
```
1. Mutual exclusion  → locks can't be shared
2. Hold and wait     → hold one, wait for another
3. No preemption     → can't force release
4. Circular wait     → A waits for B, B waits for A

Break ANY one → no deadlock.
```

**The fix — LOCK ORDERING:**
```
Both threads acquire locks in the SAME order (by account number).
  compareTo → both lock the LOWER number first.
  → no circle → one thread waits politely → no freeze ✅

It's the THREAD that waits/proceeds, not the account.
(Account = the locked resource. Thread = the worker.)
```

**SmartBank:** `transfer()` uses `compareTo` on account numbers to lock in consistent order.

---

# Step 53 — Virtual Threads (Java 21+)

```
Platform thread (= OS thread): managed by the OS, ~1MB each,
  thousands max. The old heavy thread.

Virtual thread: managed by the JVM, ~few KB each, MILLIONS possible.
  When it WAITS (I/O), it unmounts from its OS thread,
  freeing it for other work.
```

```
Restaurant analogy:
  Platform/OS thread = real waiter (expensive, few)
  Virtual thread = task ticket (cheap, millions)
  Waiters drop tickets while waiting → never idle
```

**When to use:**
```
I/O-heavy (waits for db/network/file)? → virtual threads ✅
  (waiting frees the OS thread → millions wait at once)

CPU-heavy (constant computing)?        → platform pool ✅
  (never waits → no benefit; limited by CPU cores)
```

**Don't always use virtual threads** — no benefit for CPU work, and "one per task" works only because they're too cheap to bother pooling.

**Use:** Stage 3 (Spring Boot, Java 21+): `spring.threads.virtual.enabled=true` → one server handles tens of thousands of waiting requests.

---

# Step 54 — Scheduled Tasks

**Definition:** `ScheduledExecutorService` runs tasks on a TIMER — once after a delay, or repeatedly at intervals.

```
scheduleAtFixedRate(task, initial, period, unit)
  → fires every `period` on the clock
  start ─60s─▶ run ─60s─▶ run ─60s─▶ run

scheduleWithFixedDelay(task, initial, delay, unit)
  → waits `delay` AFTER each task finishes
  run ─(finish)─delay─▶ run ─(finish)─delay─▶ run
```

**CRITICAL RULE:**
```
If a scheduled task throws an exception that escapes,
the scheduler STOPS it forever — SILENTLY.
→ ALWAYS wrap the task body in try-catch.
```

**SmartBank:** `AutoSaveService` uses `scheduleAtFixedRate` + daemon thread + dirty flag + try-catch. Saves only when data changed.

---

# THE DECISION TABLES

## Which synchronization tool?

```
┌──────────────┬─────────────────────┬─────────────────┐
│  TOOL        │  SOLVES             │  USE FOR        │
├──────────────┼─────────────────────┼─────────────────┤
│ volatile     │ visibility          │ simple flags    │
│ AtomicInteger│ visibility+atomicity│ counters (++)   │
│ synchronized │ visibility+atomicity│ check-then-act  │
└──────────────┴─────────────────────┴─────────────────┘
Lightest → heaviest: volatile < Atomic < synchronized
Use the lightest tool that solves your problem.
```

## Which collection?

```
┌──────────────────┬──────────────┬──────────────────┐
│  COLLECTION      │ LOCKING      │  BEST FOR        │
├──────────────────┼──────────────┼──────────────────┤
│ ConcurrentHashMap│ per-bucket   │ many threads     │
│ CopyOnWriteList  │ copy-on-write│ read-heavy       │
└──────────────────┴──────────────┴──────────────────┘
```

## Which thread type?

```
┌──────────────────┬──────────────────────────────────┐
│ I/O-heavy (waits)│ virtual threads (one per task)    │
│ CPU-heavy        │ platform thread pool (= cores)    │
│ simple background│ scheduled / single thread         │
└──────────────────┴──────────────────────────────────┘
```

---

# GENUINE THREADING IN SMARTBANK

```
✅ AutoSaveService    → ScheduledExecutorService, dirty flag, daemon
✅ deposit/withdraw   → synchronized (race safety)
✅ AccountRepository  → ConcurrentHashMap + CopyOnWriteArrayList
✅ transfer()         → lock ordering (deadlock prevention)

CONCEPTS FOR STAGE 3 (not in console):
  thread pools, CompletableFuture, virtual threads
```

---

# THE ONE-LINER FOR EACH STEP

```
Threads        → independent paths of execution running concurrently
Race condition → two threads touch shared data, result depends on order
synchronized   → one thread at a time (one-lane bridge)
volatile       → everyone sees the latest value (shared whiteboard)
AtomicInteger  → atomic counter (ticket machine)
ExecutorService→ reusable worker pool (small team, many tasks)
CompletableFuture→ async pipeline ("when done, then this")
ConcurrentHashMap→ self-locking map (protects its own internals)
Deadlock       → two threads each waiting for the other's lock forever
Lock ordering  → always lock in the same order → no circular wait
Virtual threads→ millions of cheap JVM threads for I/O-waiting tasks
Scheduled tasks→ run work on a timer, automatically, repeatedly
```

---

*End of Phase 5 reference. The hardest phase in pure Java — print and review before any backend interview.*