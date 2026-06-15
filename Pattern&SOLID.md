# 🏛️ Phase 4 — The Architect's Bank
## Complete Reference: SOLID Principles + Design Patterns

> SmartBank — Java Backend Learning Path
> Use this as your interview cheat sheet.

---

# PART 1 — SOLID PRINCIPLES

## S — Single Responsibility

**Definition:** A class should have ONE reason to change.

```
BEFORE:                          AFTER:
┌──────────────────┐            ┌───────────────────┐
│  AccountManager  │            │ AccountRepository │ → stores
│  - stores        │            ├───────────────────┤
│  - flags         │   split    │ RiskService       │ → flags
│  - tracks        │ ────────▶  ├───────────────────┤
│  - saves         │            │ TransactionTracker│ → tracks
│  4 jobs = 4      │            ├───────────────────┤
│  reasons to      │            │ AccountService    │ → operations
│  change          │            └───────────────────┘
└──────────────────┘            each = ONE reason to change
```

**SmartBank:** split `AccountManager` into 4 focused classes.

---

## O — Open/Closed

**Definition:** Open for EXTENSION, closed for MODIFICATION.

```
AccountType (sealed interface)
     │
     ├── Savings      ← existing
     ├── Checking     ← existing
     ├── Business     ← existing
     ├── Investment   ← ADDED — no existing code touched
     └── Premium      ← ADDED — no existing code touched

Add new type → new record.
AccountService, BankAccount → never change.
```

**SmartBank:** added Investment + Premium without editing anything.

---

## L — Liskov Substitution

**Definition:** A subtype must work anywhere its parent works without breaking behavior.

```
AccountService expects AccountType
     │
     ├── give it Savings    → works ✅
     ├── give it Premium    → works ✅
     └── give it ANY type   → works ✅

Each type honors the contract:
  valid interestRate, valid fee, valid withdrawalLimit
  enforced in compact constructors.
```

**SmartBank:** any AccountType can replace any other safely.

---

## I — Interface Segregation

**Definition:** Don't force a class to implement methods it doesn't use. Many small interfaces > one fat interface.

```
BAD:                          GOOD:
┌────────────────────┐        Displayable    → displayInfo()
│ BankBehavior       │        Transactable   → deposit/withdraw
│ - displayInfo      │        InterestBearing→ calculateInterest
│ - deposit          │        Exportable     → toCSV
│ - calculateInterest│
│ - toCSV            │        Each class implements
│ everyone forced    │        ONLY what it needs.
│ to implement ALL   │
└────────────────────┘
```

**SmartBank:** 4 small interfaces, not one giant one.

---

## D — Dependency Inversion

**Definition:** Depend on ABSTRACTIONS, not concrete classes. High-level modules shouldn't know low-level details.

```
AccountService ──▶ StoragePort ◀── DataStore (files)
 (high-level)      (interface)  ◀── DatabaseStorage (future)

AccountService never says "new DataStore()".
It talks to StoragePort.
Swap storage → AccountService unchanged.
```

**SmartBank:** AccountService depends on StoragePort, not DataStore.

---

# PART 2 — DESIGN PATTERNS

## CREATIONAL — about CREATING objects

### Singleton — "only ONE instance"

**Definition:** Guarantee one instance + global access point.

```
anyone ──▶ getInstance() ──▶ ┌─────────────┐
anyone ──▶ getInstance() ──▶ │ BankConfig  │ (same object)
anyone ──▶ getInstance() ──▶ └─────────────┘
```

- **SmartBank:** `BankConfig`
- **Trick:** private constructor blocks `new`

---

### Factory — "delegate creation to a specialist"

**Definition:** A class decides which object to build.

```
"I want SAVINGS" ──▶ ┌───────────────────┐ ──▶ new Savings()
                     │ AccountTypeFactory │
"I want PREMIUM" ──▶ │  create(choice)   │ ──▶ new Premium()
                     └───────────────────┘
```

- **SmartBank:** `AccountTypeFactory`
- Caller says WHAT, factory decides HOW to build.

---

### Builder — "construct step by step"

**Definition:** Build complex objects piece by piece, validate at end.

```
.name("Abder")
 .email("a@b.com")    ──▶ ┌──────────────┐ ──▶ validate
 .type(SAVINGS)           │   .build()   │ ──▶ BankAccount
 .deposit(5000)           └──────────────┘
```

- **SmartBank:** `BankAccountBuilder`
- Each setter returns `this` → chaining.

---

## STRUCTURAL — about CONNECTING objects

### Decorator — "wrap to add behavior"

**Definition:** Wrap an object in another implementing the same interface to add behavior, without changing the original.

```
┌─Logging────────────────┐
│  ┌─VIP─────────────┐   │
│  │  ┌──────────┐   │   │
│  │  │BankAccount│  │   │
│  │  └──────────┘   │   │
│  └─────────────────┘   │
└────────────────────────┘
```

- **SmartBank:** understood, not in final code.
- Stack wrappers freely, base class never changes.
- **Real world:** Java I/O streams, Spring Security filters.

---

## BEHAVIORAL — about HOW objects ACT and COMMUNICATE

### Observer — "one event, many reactions"

**Definition:** When one object changes, all dependents are notified automatically.

```
AccountService.publish(event)
     │
     ├──▶ AuditObserver    → logs
     ├──▶ RiskObserver     → alerts
     └──▶ TrackerObserver  → records
```

- **SmartBank:** `BankEventObserver` + 3 observers
- Subject knows nothing about who listens.

---

### Strategy — "swap the whole algorithm"

**Definition:** Define a family of algorithms, make them interchangeable at runtime.

```
BankAccount ──▶ InterestStrategy ──┬── simple
                (interface)        ├── compound
                                   ├── tiered
                                   └── zero
```

- **SmartBank:** `InterestStrategy` + `InterestStrategies`
- Each AccountType plugs in its own formula.

---

### Template Method — "fixed skeleton, variable steps"

**Definition:** Define algorithm skeleton in base class, let subclasses fill specific steps.

```
execute() {              ← final, fixed order
  header()      ← fixed
  getAmount()   ← abstract → subclass fills
  apply()       ← abstract → subclass fills
  publish()     ← fixed
}
```

- **SmartBank:** `MonthlyOperation`
  - → `MonthlyInterestOperation`
  - → `MonthlyFeesOperation`

---

### Command — "action as an object"

**Definition:** Wrap a request as an object so you can undo, queue, log, or replay it.

```
┌──DepositCommand───┐
│ execute() → do    │ ──▶ pushed to history stack
│ undo()    → revert│ ──▶ pop + undo for "undo last"
└───────────────────┘
```

- **SmartBank:** `BankCommand` + Deposit/Withdraw/Transfer + `CommandHistory`
- Enables undo. Could enable queue, schedule, replay.

---

# PART 3 — THE CONFUSION KILLER

## One-Line Test Per Pattern

```
Pattern        │ ASK YOURSELF
───────────────┼───────────────────────────────────────
Singleton      │ "Should only ONE exist?"
Factory        │ "Who picks which subclass to build?"
Builder        │ "Too many constructor params?"
Decorator      │ "Add behavior without changing the class?"
Observer       │ "One event → many reactions?"
Strategy       │ "Same task, different algorithms?"
Template       │ "Same steps in same order, details vary?"
Command        │ "Need undo, queue, or replay?"
```

## The 3 Pairs Most People Confuse

```
FACTORY vs BUILDER:
  Factory → quick, one shot: "give me a SAVINGS"
  Builder → step-by-step with optional params + validation

STRATEGY vs TEMPLATE METHOD:
  Strategy        → the WHOLE algorithm is swapped
  Template Method → only SOME STEPS swapped, skeleton stays

OBSERVER vs COMMAND:
  Observer → REACTS to events that already happened
  Command  → TRIGGERS operations as objects (with undo)
```

---

# PART 4 — PATTERNS ↔ SOLID CONNECTION

```
Singleton  → SRP (config has one home)
Factory    → SRP + OCP + DIP (creation isolated, extensible)
Builder    → SRP + LSP (construction + validation in one place)
Observer   → SRP + OCP + DIP (add reactions without changing subject)
Strategy   → OCP + DIP (add algorithm without changing context)
Decorator  → OCP + SRP + LSP (add behavior without changing class)
Template   → OCP (add operation without changing skeleton)
Command    → SRP + OCP (each operation isolated, add new freely)
```

---

# PART 5 — SMARTBANK FILE MAP

```
Singleton       → BankConfig
Factory         → AccountTypeFactory + AccountTypeChoice
Builder         → BankAccountBuilder
Observer        → BankEventObserver, AuditObserver,
                  RiskObserver, TransactionTrackerObserver
Strategy        → InterestStrategy, InterestStrategies
Decorator       → (understood, not in final code)
Template Method → MonthlyOperation, MonthlyInterestOperation,
                  MonthlyFeesOperation
Command         → BankCommand, DepositCommand, WithdrawCommand,
                  TransferCommand, CommandHistory
```

---

# PART 6 — PATTERN CATEGORIES (GoF)

```
CREATIONAL — about CREATING objects
  Singleton, Factory, Builder

STRUCTURAL — about CONNECTING objects
  Decorator
  (later: Adapter, Facade, Proxy — Stage 3)

BEHAVIORAL — about COMMUNICATION between objects
  Observer, Strategy, Template Method, Command
  (later: Chain of Responsibility — Stage 3)
```

---

*End of Phase 4 reference. Print and review before any Java interview.*