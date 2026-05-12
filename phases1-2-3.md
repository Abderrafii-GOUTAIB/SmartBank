# 🧠 Java Phases 1-3 — Interview Cheat Sheet

> **The 50 concepts you MUST know. Simple explanations. Visual schemas. Real examples.**
> Read this before any Java interview.

---

# TABLE OF CONTENTS

## Phase 1 — The Foundation
1. [JVM / JDK / JRE](#1-jvm--jdk--jre)
2. [Primitives vs References](#2-primitives-vs-references)
3. [Pass by Value](#3-pass-by-value)
4. [String Pool & Comparison](#4-string-pool--comparison)
5. [OOP — 4 Pillars](#5-oop--4-pillars)
6. [Access Modifiers](#6-access-modifiers)
7. [static vs instance](#7-static-vs-instance)
8. [final Keyword](#8-final-keyword)
9. [Constructors & this()](#9-constructors--this)
10. [Exception Hierarchy](#10-exception-hierarchy)
11. [Checked vs Unchecked](#11-checked-vs-unchecked)

## Phase 2 — The Real Bank
12. [Autoboxing & Integer Cache Trap](#12-autoboxing--integer-cache-trap)
13. [String Immutability & StringBuilder](#13-string-immutability--stringbuilder)
14. [ArrayList vs Array](#14-arraylist-vs-array)
15. [HashMap Internals](#15-hashmap-internals)
16. [equals() & hashCode() Contract](#16-equals--hashcode-contract)
17. [HashSet — How It Works](#17-hashset--how-it-works)
18. [ConcurrentModificationException](#18-concurrentmodificationexception)
19. [Comparable vs Comparator](#19-comparable-vs-comparator)
20. [Enum — Beyond Constants](#20-enum--beyond-constants)
21. [try-with-resources](#21-try-with-resources)
22. [Collections Choosing Guide](#22-collections-choosing-guide)

## Phase 3 — The Smart Bank
23. [Anonymous Class → Lambda → Method Reference](#23-anonymous-class--lambda--method-reference)
24. [Lambda Syntax & Rules](#24-lambda-syntax--rules)
25. [Effectively Final](#25-effectively-final)
26. [Method References — 4 Types](#26-method-references--4-types)
27. [Functional Interfaces — Core 9](#27-functional-interfaces--core-9)
28. [Predicate / Function / Consumer Composition](#28-predicate--function--consumer-composition)
29. [Streams — Pipeline Model](#29-streams--pipeline-model)
30. [Stream Operations Reference](#30-stream-operations-reference)
31. [Lazy Evaluation & Short-Circuiting](#31-lazy-evaluation--short-circuiting)
32. [Collectors — groupingBy, partitioningBy](#32-collectors--groupingby-partitioningby)
33. [flatMap — Flattening Nested Structures](#33-flatmap--flattening-nested-structures)
34. [Optional — Eliminating Null](#34-optional--eliminating-null)
35. [Optional Anti-Patterns](#35-optional-anti-patterns)
36. [Generics — Type Parameters](#36-generics--type-parameters)
37. [Bounded Types — extends](#37-bounded-types--extends)
38. [Wildcards — PECS](#38-wildcards--pecs)
39. [Type Erasure](#39-type-erasure)
40. [Records](#40-records)
41. [Sealed Classes](#41-sealed-classes)
42. [Immutability — 7 Rules](#42-immutability--7-rules)
43. [Defensive Copying — 3 Levels](#43-defensive-copying--3-levels)
44. [Stream vs Loop — When to Use Which](#44-stream-vs-loop--when-to-use-which)

## Cross-Cutting
45. [The Integer Division Trap](#45-the-integer-division-trap)
46. [Floating Point Money Bug](#46-floating-point-money-bug)
47. [Null Safety Strategy](#47-null-safety-strategy)
48. [Composition Over Inheritance](#48-composition-over-inheritance)
49. [Program to the Interface](#49-program-to-the-interface)
50. [The Golden Rules](#50-the-golden-rules)

---
---

# PHASE 1 — THE FOUNDATION

---

## 1. JVM / JDK / JRE

```
ANALOGY: Translation service

You speak French → Translator → Friend speaks Japanese
You write Java   → JVM       → Any computer understands

┌─────────────────────────────────────────┐
│  JDK (Development Kit)                  │
│  = everything you need to DEVELOP       │
│  ┌────────────────────────────────────┐ │
│  │  JRE (Runtime Environment)        │ │
│  │  = everything needed to RUN       │ │
│  │  ┌─────────────────────────────┐  │ │
│  │  │  JVM (Virtual Machine)      │  │ │
│  │  │  = the engine that executes │  │ │
│  │  └─────────────────────────────┘  │ │
│  │  + Standard Library (String, etc) │ │
│  └────────────────────────────────────┘ │
│  + javac (compiler) + tools             │
└─────────────────────────────────────────┘

Your Code (.java) → javac → Bytecode (.class) → JVM → Execution
                    compile                      run
```

**Interview key:** Different JVM per OS, same bytecode. "Write Once, Run Anywhere."

---

## 2. Primitives vs References

```
ANALOGY:
  Primitive = a sticky note with a number written on it (the VALUE itself)
  Reference = a sticky note with an ADDRESS written on it (points to a house)

PRIMITIVES (stored on stack):          REFERENCES (object on heap):
┌──────┐                               ┌──────┐      ┌──────────┐
│ a: 5 │  ← the value itself           │ x: @ │ ───→ │ {1, 2, 3}│
└──────┘                               └──────┘      └──────────┘

int a = 5;                             int[] x = {1, 2, 3};
int b = a;  ← COPIES the value        int[] y = x;  ← COPIES the address
b = 99;                                y[0] = 99;
a is still 5 ✅                         x[0] is now 99 😱 (same object!)
```

**8 primitives:** `byte, short, int, long, float, double, boolean, char`
**Everything else** is a reference: String, arrays, objects, collections.

---

## 3. Pass by Value

```
ANALOGY:
  Primitive parameter = photocopy of your sticky note (change the copy, original unchanged)
  Object parameter    = photocopy of the ADDRESS (both point to same house)

void change(int x)     { x = 99; }         → original UNCHANGED (copy of value)
void change(List list)  { list.add("hi"); }  → original MODIFIED (same object)
void change(List list)  { list = new ...; }  → original UNCHANGED (redirected the copy)
```

**Interview key:** Java is ALWAYS pass-by-value. For objects, the "value" being passed is the reference (address).

---

## 4. String Pool & Comparison

```
ANALOGY: String Pool = a library of reusable books
  "Hello" → library has one copy → everyone reads the SAME copy
  new String("Hello") → you buy your OWN copy from the bookstore

String a = "Hello";           ← from the pool
String b = "Hello";           ← SAME object from pool
String c = new String("Hello"); ← NEW object, different address

a == b     → true  (same pool object)
a == c     → false (different objects!)
a.equals(c) → true  (same CONTENT) ✅

⚠️ RULE: ALWAYS use .equals() for String comparison. NEVER use ==.
```

**Interview trap:** `==` works in testing (both literals from pool) but breaks with user input, file reading, `new String()`, database results.

---

## 5. OOP — 4 Pillars

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│  ENCAPSULATION — "Hide the wiring, expose the buttons"          │
│  ─────────────                                                  │
│  ANALOGY: Vending machine. You press buttons (methods),         │
│  you can't reach inside (private fields).                       │
│                                                                 │
│  private double balance;      ← hidden                          │
│  public void deposit(amount)  ← controlled access               │
│  // No setBalance()! Only deposit() and withdraw()              │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  INHERITANCE — "Is-a relationship"                              │
│  ───────────                                                    │
│  ANALOGY: Student IS-A Person. Has everything a Person has,     │
│  plus studentId and gpa.                                        │
│                                                                 │
│  class Student extends Person { String studentId; }             │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  POLYMORPHISM — "Same message, different behavior"              │
│  ─────────────                                                  │
│  ANALOGY: "Make a sound" → Dog barks, Cat meows, Duck quacks.   │
│  Same method call, different result depending on actual object.  │
│                                                                 │
│  Animal pet = new Dog();                                        │
│  pet.makeSound(); → "Woof!" (Dog's version, not Animal's)      │
│                                                                 │
│  Compile time: variable type → determines WHAT you can call     │
│  Runtime:      object type  → determines WHICH version runs     │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ABSTRACTION — "What, not how"                                  │
│  ────────────                                                   │
│  ANALOGY: Steering wheel. Turn left → car goes left.            │
│  You don't need to know about power steering pumps.             │
│                                                                 │
│  interface Displayable { void displayInfo(); }                  │
│  // Contract says WHAT to do. Each class decides HOW.           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 6. Access Modifiers

```
                 Same    Same      Sub-      Any
Modifier         Class   Package   class     class
──────────────────────────────────────────────────
private           ✅      ❌        ❌        ❌     "Only me"
(default)         ✅      ✅        ❌        ❌     "My floor"
protected         ✅      ✅        ✅        ❌     "My floor + children"
public            ✅      ✅        ✅        ✅     "Everyone"

RULE: Start private. Only widen when you have a reason.
```

---

## 7. static vs instance

```
ANALOGY:
  static  = the STORE's ticket counter (shared by all customers)
  instance = each CUSTOMER's shopping cart (personal to each)

┌─────────────────────────────┐
│ CLASS: BankAccount          │
│ static int nextId = 4      │ ← ONE copy, shared
│ static String BANK_NAME    │ ← ONE copy, constant
└─────────────────────────────┘
     │           │           │
┌────┴──┐   ┌───┴───┐   ┌───┴───┐
│ACC-001│   │ACC-002│   │ACC-003│  ← EACH has its own
│$2500  │   │$800   │   │$0     │    instance fields
└───────┘   └───────┘   └───────┘

static: belongs to CLASS    → BankAccount.BANK_NAME
instance: belongs to OBJECT → account.getBalance()
```

---

## 8. final Keyword

```
ANALOGY:
  final variable = name written in permanent marker (can't erase and rewrite)
  final on a list = the LABEL is permanent, but the BOX contents can change

final int MAX = 100;           → can't reassign MAX ✅
final List<String> names = ...; → can't reassign 'names' to new list
                                → CAN add/remove from the list!

⚠️ final controls the REFERENCE, not the OBJECT.

final variable → can't reassign
final method   → can't override in subclass
final class    → can't extend (no subclasses)
```

---

## 9. Constructors & this()

```
ANALOGY: this() = "call the master recipe"
  Instead of duplicating the recipe in 3 cookbooks,
  2 cookbooks say "follow the master recipe with these defaults."

BankAccount(String name, String email, double balance) {
    // ALL validation here — single source of truth
    this.name = name;  // 'this' = "my field, not the parameter"
}

BankAccount(String name, String email) {
    this(name, email, 0.0);  // calls the master constructor
}

BankAccount(BankAccount other) {
    this(other.name, other.email, 0.0);  // copy constructor
}

WHY: Change validation once → all constructors get the fix. DRY.
```

---

## 10. Exception Hierarchy

```
Throwable
├── Error ← JVM problems (OutOfMemory, StackOverflow) — DON'T catch
└── Exception
    ├── IOException         ← Checked (compiler forces handling)
    ├── SQLException        ← Checked
    └── RuntimeException    ← Unchecked (programming bugs)
        ├── NullPointerException
        ├── IllegalArgumentException
        ├── ClassCastException
        └── IndexOutOfBoundsException

CUSTOM HIERARCHY (what we built):
BankException (RuntimeException)
├── AccountException
│   ├── AccountNotFoundException
│   ├── AccountFrozenException
│   └── DuplicateEmailException
└── TransactionException
    ├── InsufficientFundsException
    ├── InvalidAmountException
    └── DailyLimitExceededException
```

---

## 11. Checked vs Unchecked

```
CHECKED (extends Exception):
  "The caller can RECOVER from this."
  File not found → try another file
  Network down → retry later
  Compiler FORCES try/catch or throws

UNCHECKED (extends RuntimeException):
  "This is a PROGRAMMING BUG. Fix the code."
  Null pointer → don't pass null
  Bad argument → validate before calling
  Compiler does NOT force handling

DECISION:
  External failure (disk, network, DB) → checked
  Business rule violation → unchecked
  Programming bug → unchecked
```

---
---

# PHASE 2 — THE REAL BANK

---

## 12. Autoboxing & Integer Cache Trap

```
ANALOGY:
  Autoboxing = putting a present in a gift box automatically
  Unboxing   = taking it out automatically

int x = 42;
Integer boxed = x;      ← autoboxing (int → Integer)
int unboxed = boxed;    ← unboxing (Integer → int)

⚠️ THE TRAP:
Integer a = 127;
Integer b = 127;
a == b → true   ← Java CACHES -128 to 127 (same object!)

Integer c = 128;
Integer d = 128;
c == d → false  ← Outside cache! Different objects!

RULE: ALWAYS use .equals() for wrapper comparison. Same rule as String.
```

---

## 13. String Immutability & StringBuilder

```
ANALOGY:
  String = carved in stone. To "change" it, carve a new stone.
  StringBuilder = whiteboard. Erase and rewrite freely.

String s = "Hello";
s = s + " World";    ← creates NEW String object, old one = garbage

❌ BAD (10,000 new String objects):
String result = "";
for (int i = 0; i < 10000; i++) { result += i; }

✅ GOOD (1 mutable buffer):
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 10000; i++) { sb.append(i); }
String result = sb.toString();

PATTERN: Build with StringBuilder, return as String.
         StringBuilder for building (efficient).
         String for sharing (immutable, safe).
```

---

## 14. ArrayList vs Array

```
ARRAY (fixed):                    ARRAYLIST (dynamic):
┌───┬───┬───┬───┬───┐            ┌───┬───┬───┐
│ A │ B │ C │   │   │            │ A │ B │ C │  ← only uses what it needs
└───┴───┴───┴───┴───┘            └───┴───┴───┘
  Size: ALWAYS 5                   Size: 3 (grows automatically)
  arr[i]                           list.get(i)
  arr.length                       list.size()
  Can't grow                       .add() expands
  Can't remove                     .remove() shrinks
  Manual counting                  Tracks itself
```

**Interview key:** Always declare as `List<T>` (interface), not `ArrayList<T>` (implementation). "Program to the interface."

---

## 15. HashMap Internals

```
ANALOGY: Dictionary lookup
  You don't read every page to find "Elephant."
  You jump to "E" section → find it instantly.

HOW IT WORKS:
  map.get("ACC-0002")

  Step 1: "ACC-0002".hashCode() → 798342     (crunch key to number)
  Step 2: 798342 % 16 → bucket 6             (which slot)
  Step 3: Go to bucket 6                      (instant jump)
  Step 4: Check .equals() for exact match     (confirm identity)
  Step 5: Return value                        (found!)

┌──────────┐
│ Bucket 0 │ → empty
│ Bucket 1 │ → "ACC-0003" → Carol
│ Bucket 4 │ → "ACC-0001" → Alice
│ Bucket 6 │ → "ACC-0002" → Bob
└──────────┘

ArrayList search: O(n) — checks every element
HashMap lookup:   O(1) — instant, regardless of size
```

---

## 16. equals() & hashCode() Contract

```
THE CONTRACT (violate this → HashMap breaks):

  IF a.equals(b) is true  → a.hashCode() == b.hashCode() MUST be true
  IF hashCodes differ      → equals MUST be false
  Same hashCode            → equals MAY be true or false (collisions OK)

ANALOGY:
  hashCode = which NEIGHBORHOOD to look in
  equals   = which HOUSE on the street

  Same neighborhood doesn't mean same house (collision).
  But same house MUST be in the same neighborhood.

class Student {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student s)) return false;
        return Objects.equals(id, s.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);  // MUST use same fields as equals
    }
}

⚠️ If you override equals, you MUST override hashCode. Always.
```

---

## 17. HashSet — How It Works

```
TRUTH: HashSet is just a HashMap where you only care about keys.

set.add("Alice")  → internally: map.put("Alice", DUMMY_VALUE)
set.contains("X") → internally: map.containsKey("X")

THREE IMPLEMENTATIONS:
  HashSet        → unordered, fastest O(1)
  LinkedHashSet  → insertion order preserved
  TreeSet        → sorted order, O(log n)
```

---

## 18. ConcurrentModificationException

```
ANALOGY: You're counting people in a room.
  Someone opens the door and lets 3 more in while you're counting.
  Your count is now wrong. Java throws an exception to prevent this.

❌ BAD: modify list while iterating with for-each
for (Account acc : accounts) {
    if (acc.getBalance() <= 0) accounts.remove(acc);  // 💥 CRASH
}

✅ FIX 1: Iterator.remove()
Iterator<Account> it = accounts.iterator();
while (it.hasNext()) {
    if (it.next().getBalance() <= 0) it.remove();  // safe
}

✅ FIX 2: removeIf (one-liner)
accounts.removeIf(acc -> acc.getBalance() <= 0);

✅ FIX 3: Collect-then-remove
List<Account> toRemove = new ArrayList<>();
for (Account acc : accounts) {
    if (acc.getBalance() <= 0) toRemove.add(acc);
}
accounts.removeAll(toRemove);
```

---

## 19. Comparable vs Comparator

```
COMPARABLE = "I know my own natural order" (built INTO the class)
  class Transaction implements Comparable<Transaction> {
      public int compareTo(Transaction other) {
          return Integer.compare(this.id, other.id);
      }
  }
  Collections.sort(transactions);  // uses compareTo automatically

COMPARATOR = "Here's a different way to sort" (EXTERNAL)
  Comparator<Account> byBalance = Comparator.comparingDouble(Account::getBalance);
  Comparator<Account> byName = Comparator.comparing(Account::getOwnerName);
  accounts.sort(byBalance.reversed().thenComparing(byName));

RULE:
  Comparable → ONE natural obvious order (Transaction by ID)
  Comparator → MULTIPLE possible orders (Account by balance, name, status...)
  If you can't answer "what's THE order?" → don't use Comparable
```

---

## 20. Enum — Beyond Constants

```
ANALOGY: Enums are not just labels. They're full classes with a fixed number of instances.

public enum TransactionType {
    DEPOSIT("Deposit", +1),     // each constant = an instance
    WITHDRAWAL("Withdrawal", -1);

    private final String displayName;
    private final int sign;

    TransactionType(String displayName, int sign) {  // private constructor
        this.displayName = displayName;
        this.sign = sign;
    }

    public boolean isCredit() { return sign > 0; }
    public String formatAmount(double amt) { return (isCredit() ? "+" : "-") + "$" + amt; }
}

ENUM STATE MACHINE (each constant has different behavior):
  ACTIVE   { boolean canTransitionTo(Status s) { return s == FROZEN || s == CLOSED; } }
  FROZEN   { boolean canTransitionTo(Status s) { return s == ACTIVE || s == CLOSED; } }
  CLOSED   { boolean canTransitionTo(Status s) { return false; } }  // terminal

WHY ENUM > INT CONSTANTS:
  int status = 999;           → compiles, crashes at runtime
  AccountStatus.ACTIVE        → compiler rejects invalid values
  status.canTransact()        → behavior lives WITH the data
```

---

## 21. try-with-resources

```
ANALOGY: A hotel room that automatically checks you out at the end.

❌ OLD WAY (verbose, error-prone):
BufferedReader reader = null;
try {
    reader = new BufferedReader(new FileReader("data.txt"));
    // use reader...
} finally {
    if (reader != null) reader.close();  // must not forget!
}

✅ NEW WAY (auto-closes):
try (var reader = new BufferedReader(new FileReader("data.txt"))) {
    // use reader...
}  // reader.close() called automatically, even on exception

Works with any class implementing AutoCloseable.
```

---

## 22. Collections Choosing Guide

```
"What do I need?"

Ordered + index access?     → ArrayList
Ordered + fast insert/remove? → LinkedList
No duplicates?              → HashSet (unordered) / LinkedHashSet (ordered) / TreeSet (sorted)
Key → Value lookup?         → HashMap (unordered) / LinkedHashMap (ordered) / TreeMap (sorted)
Thread-safe map?            → ConcurrentHashMap
Queue (FIFO)?               → ArrayDeque
Priority queue?             → PriorityQueue

COMMON COMBO: ArrayList + HashMap together
  List for ordered display, Map for instant lookup.
```

---
---

# PHASE 3 — THE SMART BANK

---

## 23. Anonymous Class → Lambda → Method Reference

```
THE EVOLUTION (same behavior, less ceremony):

STEP 1 — Named class (7+ lines):
class DepositFilter implements TransactionFilter {
    @Override
    public boolean accept(Transaction t) {
        return t.getType() == TransactionType.DEPOSIT;
    }
}
TransactionFilter f = new DepositFilter();

STEP 2 — Anonymous class (5 lines):
TransactionFilter f = new TransactionFilter() {
    @Override
    public boolean accept(Transaction t) {
        return t.getType() == TransactionType.DEPOSIT;
    }
};

STEP 3 — Lambda (1 line):
TransactionFilter f = t -> t.getType() == TransactionType.DEPOSIT;

STEP 4 — Method reference (when applicable):
Function<Transaction, TransactionType> f = Transaction::getType;
```

---

## 24. Lambda Syntax & Rules

```
FORMS (from verbose to concise):

(Transaction t) -> { return t.getAmount() > 500; }   // full
(t) -> { return t.getAmount() > 500; }               // type inference
t -> { return t.getAmount() > 500; }                  // single param: no parens
t -> t.getAmount() > 500                              // single expression: no braces, no return
() -> System.out.println("Hello")                     // no params

RULES:
  ✅ Lambdas work ONLY with functional interfaces (1 abstract method)
  ✅ Parameter types are inferred from the interface
  ✅ Captured variables must be effectively final
  ❌ Cannot replace: multi-method interfaces, abstract classes, need for fields/state
```

---

## 25. Effectively Final

```
ANALOGY: A lambda takes a PHOTOGRAPH of a variable.
  If the variable changes after the photo, the photo is wrong.
  Java prevents this confusion entirely.

String prefix = "Hello";   // never reassigned → effectively final
Runnable r = () -> System.out.println(prefix);  // ✅ captures "Hello"

String greeting = "Hi";
greeting = "Hey";          // reassigned!
Runnable r = () -> System.out.println(greeting);  // ❌ COMPILE ERROR

WHY: Lambda captures a COPY of the value.
     If the variable changed later, the copy would be stale.
     Java prevents the confusion by requiring effectively final.
```

---

## 26. Method References — 4 Types

```
TYPE                    LAMBDA                        METHOD REF
────────────────────────────────────────────────────────────────
1. Static method        x -> Math.abs(x)              Math::abs
2. Instance on object   x -> System.out.println(x)    System.out::println
3. Instance on param    x -> x.toUpperCase()          String::toUpperCase
4. Constructor          () -> new ArrayList<>()        ArrayList::new

RULE: Use method ref ONLY when lambda does nothing except call one method.
  ✅ t -> t.getAmount()              → Transaction::getAmount
  ❌ t -> t.getAmount() > 500        → KEEP lambda (has logic)
  ❌ t -> ">> " + t.toString()       → KEEP lambda (transforms)
```

---

## 27. Functional Interfaces — Core 9

```
INTERFACE             SIGNATURE          ENGLISH              EXAMPLE
─────────────────────────────────────────────────────────────────────
Predicate<T>          T → boolean        "Test this"          Is balance > 1000?
Function<T, R>        T → R              "Transform this"     Account → String name
Consumer<T>           T → void           "Use this"           Print the account
Supplier<T>           () → T             "Give me one"        Create new ArrayList

UnaryOperator<T>      T → T              "Transform in-place" Trim a string
BinaryOperator<T>     (T,T) → T          "Combine two"        Add two balances
BiFunction<T,U,R>     (T,U) → R          "Two in, one out"    (name, bal) → Account
BiPredicate<T,U>      (T,U) → boolean    "Test two things"    (acc, min) → bal > min
BiConsumer<T,U>       (T,U) → void       "Use two things"     (key, val) → print

MEMORY TRICK:
  Predicate  → P for "Pass/fail?" (boolean)
  Function   → F for "From T to R" (transform)
  Consumer   → C for "Consume and done" (void)
  Supplier   → S for "Supply from nothing" (factory)
```

---

## 28. Predicate / Function / Consumer Composition

```
PREDICATE CHAINING:
  Predicate<Account> active = Account::isActive;
  Predicate<Account> rich = a -> a.getBalance() > 1000;

  active.and(rich)       → both must be true
  active.or(rich)        → either can be true
  active.negate()        → flip (NOT active)

FUNCTION CHAINING:
  Function<Account, String> getName = Account::getOwnerName;
  Function<String, String> toUpper = String::toUpperCase;

  getName.andThen(toUpper)  → getName FIRST, then toUpper
  toUpper.compose(getName)  → getName FIRST, then toUpper (reverse syntax)

CONSUMER CHAINING:
  Consumer<Account> printName = a -> System.out.println(a.getName());
  Consumer<Account> printBal  = a -> System.out.println(a.getBalance());

  printName.andThen(printBal)  → do BOTH, in order
```

---

## 29. Streams — Pipeline Model

```
ANALOGY: A factory assembly line.
  Raw materials → Station 1 (filter) → Station 2 (transform) → Station 3 (package) → Product

SOURCE              INTERMEDIATE (lazy)         TERMINAL (triggers all)
──────              ───────────────────         ─────────────────────
accounts.stream()   .filter(Account::isActive)  .toList()
                    .map(Account::getName)      .forEach(System.out::println)
                    .sorted()                   .count()
                    .distinct()                 .sum()
                    .limit(10)                  .findFirst()
                                                .collect(Collectors.groupingBy(...))

KEY PROPERTIES:
  • Lazy — nothing happens until terminal operation
  • Single-use — a stream can only be consumed once
  • Non-mutating — original collection unchanged
  • Short-circuiting — findFirst/anyMatch can stop early
```

---

## 30. Stream Operations Reference

```
INTERMEDIATE (return Stream — lazy):
  .filter(Predicate)      keep matching
  .map(Function)          transform each element
  .flatMap(Function)      flatten nested streams
  .sorted()               natural order
  .sorted(Comparator)     custom order
  .distinct()             remove duplicates
  .limit(n)               first n elements
  .skip(n)                skip first n
  .peek(Consumer)         debug (look without changing)

TERMINAL (trigger pipeline — eager):
  .forEach(Consumer)       do something with each
  .toList()                collect to immutable list
  .collect(Collector)      collect to any structure
  .count()                 count elements
  .findFirst()             first element (Optional)
  .anyMatch(Predicate)     any match? (boolean)
  .allMatch(Predicate)     all match? (boolean)
  .noneMatch(Predicate)    none match? (boolean)
  .min(Comparator)         smallest (Optional)
  .max(Comparator)         largest (Optional)
  .reduce(identity, op)    combine all into one

NUMERIC:
  .mapToDouble(fn) → DoubleStream → .sum(), .average(), .max(), .min()
  .summaryStatistics() → count + sum + min + max + average in ONE pass
```

---

## 31. Lazy Evaluation & Short-Circuiting

```
List.of("Alice", "Bob", "Charlie", "David").stream()
    .filter(n -> { print("filter: " + n); return n.length() > 3; })
    .map(n -> { print("map: " + n); return n.toUpperCase(); })
    .findFirst();

OUTPUT:
  filter: Alice    ← passes (length 5 > 3)
  map: Alice       ← transforms to "ALICE"
  → returns "ALICE"

Bob, Charlie, David are NEVER processed!
findFirst() stops the pipeline after the first match.
With 1 million elements, it might only process 1.
```

---

## 32. Collectors — groupingBy, partitioningBy

```
GROUPBY — split into N groups by key:
  accounts.stream().collect(
      Collectors.groupingBy(Account::getStatus))
  → {ACTIVE: [Alice, Bob], FROZEN: [Carol]}

GROUPBY + DOWNSTREAM — group AND aggregate:
  .collect(groupingBy(Account::getStatus, counting()))
  → {ACTIVE: 2, FROZEN: 1}

  .collect(groupingBy(Account::getStatus, summingDouble(Account::getBalance)))
  → {ACTIVE: 5000.0, FROZEN: 800.0}

PARTITIONING — split into exactly 2 groups (true/false):
  .collect(partitioningBy(a -> a.getBalance() > 1000))
  → {true: [rich accounts], false: [poor accounts]}
  Both keys ALWAYS present (even if empty list).

DIFFERENCE:
  groupingBy    → N groups, missing keys if no matches
  partitioningBy → exactly 2 groups, both always present
```

---

## 33. flatMap — Flattening Nested Structures

```
PROBLEM: Each account has a List<Transaction>.
  .map(acc -> acc.getTransactions()) → Stream<List<Transaction>> ← nested!

SOLUTION: flatMap unwraps and merges:
  .flatMap(acc -> acc.getTransactions().stream()) → Stream<Transaction> ← flat!

VISUAL:
  accounts: [Alice, Bob, Carol]
  
  .map(getTransactions):
    [ [txn1, txn2], [txn3], [txn4, txn5] ]  ← list of lists 😱

  .flatMap(getTransactions().stream()):
    [ txn1, txn2, txn3, txn4, txn5 ]  ← flat stream ✅

ANALOGY: map = open each box, look inside
         flatMap = open each box, DUMP contents into one pile
```

---

## 34. Optional — Eliminating Null

```
ANALOGY: A gift box that might be empty.
  Instead of handing someone a present (or nothing/null),
  you hand them a BOX — they can check if it's empty safely.

CREATING:
  Optional.of(value)           value MUST NOT be null
  Optional.ofNullable(value)   value CAN be null → empty if null
  Optional.empty()             explicitly empty

CONSUMING (the RIGHT way):
  opt.ifPresent(acc -> ...)                    do something if present
  opt.ifPresentOrElse(acc -> ..., () -> ...)   handle both cases
  opt.orElse(defaultValue)                     unwrap or use default
  opt.orElseGet(() -> expensiveCreate())       unwrap or compute (lazy)
  opt.orElseThrow(() -> new NotFoundException()) unwrap or throw

TRANSFORMING:
  findAccount("ACC-0001")           // Optional<Account>
      .map(Account::getBalance)     // Optional<Double>
      .map(bal -> "$" + bal)        // Optional<String>
      .orElse("Not found");         // String

  // Each .map() transforms the value IF present
  // If any step is empty → all subsequent maps are skipped → orElse runs

RULE: Optional is for RETURN TYPES only.
      Not fields. Not parameters. Not collections.
```

---

## 35. Optional Anti-Patterns

```
❌ ANTI-PATTERN 1: isPresent() + get() — null check in disguise
  if (opt.isPresent()) { opt.get(); }
  ✅ Use: opt.ifPresent(acc -> ...)

❌ ANTI-PATTERN 2: Optional as field
  class Account { Optional<String> middleName; }
  ✅ Use null for fields, Optional for return types

❌ ANTI-PATTERN 3: Optional as parameter
  void deposit(Optional<Double> amount)
  ✅ Overload the method instead

❌ ANTI-PATTERN 4: Optional.of(possiblyNull)
  Optional.of(null) → NullPointerException!
  ✅ Use Optional.ofNullable(possiblyNull)

❌ ANTI-PATTERN 5: Optional<List<T>>
  ✅ Just return empty list — collections represent emptiness
```

---

## 36. Generics — Type Parameters

```
ANALOGY: A form with blanks.
  "Filter a list of _____" → fill in BankAccount, Transaction, String

class Repository<T> {          // T = type parameter (blank)
    List<T> items;
    void add(T item) { ... }
    Optional<T> findFirst(Predicate<T> condition) { ... }
    <R> List<R> map(Function<T, R> mapper) { ... }
}

Repository<BankAccount> repo;  // T = BankAccount (filled in)
Repository<Transaction> txns;  // T = Transaction (different fill)
Repository<String> names;      // T = String (same class, different type)

T = class-level type (decided at creation)
R = method-level type (decided at each call, for different return type)
K = key type (convention for maps/grouping)
```

---

## 37. Bounded Types — extends

```
PROBLEM: <T> means T can be ANYTHING. Can't call T-specific methods.

public static <T> T findMax(List<T> list) {
    item.compareTo(other)  // ❌ ERROR — T might not have compareTo!
}

FIX: Restrict T to types that have compareTo:

public static <T extends Comparable<T>> T findMax(List<T> list) {
    item.compareTo(other)  // ✅ guaranteed — T must be Comparable
}

WHEN TO BOUND:
  Method calls .compareTo()  → <T extends Comparable<T>>
  Method calls .displayInfo() → <T extends Displayable>
  Method calls .getBalance()  → <T extends BankAccount>
  Method just stores/passes T → no bound needed: <T>
```

---

## 38. Wildcards — PECS

```
PECS = Producer Extends, Consumer Super

PRODUCER (you READ from it):
  List<? extends Number> numbers
  Number n = numbers.get(0);   ✅ can READ as Number
  numbers.add(42);              ❌ can't WRITE (might be List<Double>)

CONSUMER (you WRITE to it):
  List<? super Integer> target
  target.add(42);               ✅ can WRITE Integer
  Integer n = target.get(0);    ❌ can only READ as Object

MEMORY TRICK:
  ? extends T → read-only view (PRODUCER gives you items)
  ? super T   → write-only view (CONSUMER takes your items)

PRACTICAL:
  void copy(List<? extends T> source, List<? super T> dest) {
      for (T item : source) dest.add(item);
  }
  // READ from source (extends), WRITE to dest (super)
```

---

## 39. Type Erasure

```
Generics exist at COMPILE TIME only. At runtime, they disappear.

List<String> strings = new ArrayList<>();
List<Integer> integers = new ArrayList<>();
strings.getClass() == integers.getClass()  → true!
// Both are just ArrayList at runtime

CONSEQUENCES:
  ❌ if (obj instanceof List<String>)  — type gone at runtime
  ❌ new T()                           — JVM doesn't know what T is
  ❌ new T[10]                         — can't create generic arrays
  ❌ List<String> vs List<Integer>     — indistinguishable at runtime

Generics = compile-time safety net. Prevents bugs BEFORE code runs.
At runtime, they vanish completely.
```

---

## 40. Records

```
ANALOGY: A pre-printed form. Fill in the fields, everything else is automatic.

// 50+ lines as a class → 1 line as a record:
public record AccountSummary(String accountNumber, String name, double balance) {}

AUTO-GENERATED:
  ✅ private final fields
  ✅ Constructor
  ✅ Accessors: .accountNumber(), .name(), .balance() (no "get" prefix!)
  ✅ equals() based on ALL fields
  ✅ hashCode() based on ALL fields
  ✅ toString() showing ALL fields

CAN HAVE:
  ✅ Custom methods, static methods, static fields
  ✅ Implement interfaces
  ✅ Compact constructors (validation)
  ✅ Override toString/equals/hashCode

CANNOT:
  ❌ Extend a class (implicitly extends Record)
  ❌ Have mutable fields (all final)
  ❌ Have extra instance fields beyond components

USE WHEN: Immutable data that doesn't change after creation.
  Transaction receipt, API response, search result, event.
```

---

## 41. Sealed Classes

```
ANALOGY: A private party with a guest list.
  Only people on the list can enter. No exceptions.

sealed interface AccountType
    permits Savings, Checking, Business { }

record Savings(double rate) implements AccountType {}
record Checking(double fee) implements AccountType {}
record Business(double rate, double fee) implements AccountType {}
// Nobody else can implement AccountType!

THE POWER — exhaustive switch (no default needed):
double fee(AccountType type) {
    return switch (type) {
        case Savings s   -> 0;
        case Checking c  -> c.fee();
        case Business b  -> b.fee();
        // No default! Compiler KNOWS these are the only 3.
        // Add a 4th type → this code WON'T COMPILE until updated.
    };
}

SUBCLASS MODIFIERS:
  final      → chain stops here (nobody extends)
  sealed     → controls its own subtypes (multi-level)
  non-sealed → open for extension (back to normal)
```

---

## 42. Immutability — 7 Rules

```
To make a class immutable:

  1. Class is FINAL              (no subclasses adding mutable fields)
  2. All fields PRIVATE + FINAL  (set once, never again)
  3. NO setter methods           (no setBalance, no setName)
  4. Defensive copy INPUTS       (this.list = List.copyOf(inputList))
  5. Defensive copy OUTPUTS      (return List.copyOf(this.list))
  6. All contained objects also IMMUTABLE (List of immutable objects)
  7. "Modification" returns NEW object (withBalance returns new Account)

ALREADY IMMUTABLE IN JAVA:
  String, Integer, Double, Boolean, Long
  LocalDate, LocalTime, LocalDateTime
  BigDecimal, Enum constants, Records
  Optional, List.of(), Set.of(), Map.of()

MUTABLE (need defensive copy):
  ArrayList, HashMap, HashSet, arrays, Date, StringBuilder
```

---

## 43. Defensive Copying — 3 Levels

```
LEVEL 1 — return reference (BROKEN):
  return this.transactions;
  → Caller can .clear() your data 💀

LEVEL 2 — unmodifiable view (PARTIAL):
  return Collections.unmodifiableList(transactions);
  → Caller can't modify through VIEW
  → But YOUR internal changes are visible to caller 💀

LEVEL 3 — immutable copy (SAFE):
  return List.copyOf(transactions);
  → Independent copy. Can't modify. Internal changes invisible. ✅
  → If objects inside are also immutable → FULLY safe

WHEN NEEDED:
  String, enum, LocalDateTime, primitives → return directly (already immutable)
  List, Map, Set, arrays                  → List.copyOf() / defensive copy
```

---

## 44. Stream vs Loop — When to Use Which

```
USE STREAM WHEN:                    USE LOOP WHEN:
  ✅ Readability matters             ✅ Proven performance hot path
  ✅ Data transformation pipeline    ✅ Need to break/continue with complex logic
  ✅ Filtering + mapping + collecting ✅ Modifying external state in each iteration
  ✅ Parallelism potential           ✅ Index-based access needed
  ✅ Composing operations            ✅ Simple iteration (no transformation)

PERFORMANCE REALITY:
  Streams have ~10-30% overhead in micro-benchmarks.
  In real apps: negligible (<1% of total time).
  DB calls, network I/O, business logic take 99%+ of time.

SENIOR ANSWER: "Readability over micro-optimization.
  I'd only drop to a loop in a proven hot path after profiling."
```

---
---

# CROSS-CUTTING CONCEPTS

---

## 45. The Integer Division Trap

```
10 / 3 = 3      (not 3.33! both operands are int → int result → truncated)
10.0 / 3 = 3.33  (one double operand → double result → correct)

int cents = 999;
int dollars = cents / 100;  → 9 (not 9.99!)

FIX: make at least one operand double.
```

---

## 46. Floating Point Money Bug

```
0.1 + 0.2 = 0.30000000000000004   ← NOT 0.3!

After 10,000 transactions, balances drift by pennies.
Audit fails: sum of transactions ≠ final balance.

FIX: Use BigDecimal for money, or store as cents (long).
```

---

## 47. Null Safety Strategy

```
ERA 1 — Raw null (dangerous):
  Account acc = find(id);
  if (acc != null) { ... }     ← easy to forget!

ERA 2 — Throw exception (heavy):
  try { find(id); }
  catch (NotFoundException e) { ... }

ERA 3 — Optional (safe, modern):
  find(id)
    .map(Account::getBalance)
    .orElse(0.0);              ← compiler guides you

USE:
  Absence is ERROR → throw exception
  Absence is NORMAL → return Optional
  Collections → return empty list (never Optional<List>)
```

---

## 48. Composition Over Inheritance

```
❌ Inheritance problem:
  Bird extends Animal → fly()
  Penguin extends Bird → Penguins can't fly! 💀

✅ Composition solution:
  interface Flyable { void fly(); }
  interface Swimmable { void swim(); }

  class Duck implements Flyable, Swimmable { ... }   // can fly + swim
  class Penguin implements Swimmable { ... }           // swim only, no broken contract

RULE: Inherit for identity (IS-A). Compose for capability (CAN-DO).
```

---

## 49. Program to the Interface

```
❌ BAD: Tied to implementation
  ArrayList<Account> accounts = new ArrayList<>();

✅ GOOD: Tied to interface
  List<Account> accounts = new ArrayList<>();

WHY: You can swap ArrayList for LinkedList, CopyOnWriteArrayList,
or any List implementation — zero code changes.

SAME PRINCIPLE EVERYWHERE:
  Map<K,V> map = new HashMap<>();        (not HashMap<K,V>)
  Set<T> set = new HashSet<>();          (not HashSet<T>)
  void process(Displayable item) { ... } (not BankAccount)
```

---

## 50. The Golden Rules

```
1. .equals() for comparison, never == (for objects)
2. private by default, widen only when needed
3. final by default for fields that shouldn't change
4. List.copyOf() for returning collections
5. Optional for return types that might be empty
6. Streams for data transformation pipelines
7. Records for immutable data carriers
8. Sealed classes for controlled type hierarchies
9. @FunctionalInterface for single-method interfaces
10. Composition over inheritance
11. Program to the interface
12. Effectively final for lambda captures
13. StringBuilder in loops, String everywhere else
14. BigDecimal for money, never double
15. java.time for dates, never java.util.Date
```