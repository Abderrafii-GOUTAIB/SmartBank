# SmartBank

SmartBank is a console banking application built in pure Java. It demonstrates
clean object-oriented design, SOLID principles, classic design patterns, and
safe concurrency — modelling accounts, transactions, transfers, interest and
fee cycles, risk flagging, and persistent storage from a single interactive
terminal menu.

## Requirements

- JDK 21
- Maven 3.8+

## Build & Run

```bash
# Build the project (compiles and installs the artifact)
mvn clean install

# Run the unit tests
mvn test

# Run the application
mvn exec:java -Dexec.mainClass=org.example.app.Main
# or, after `mvn clean install`:
java -cp target/classes org.example.app.Main
```

## Key concepts demonstrated

- **SOLID principles** — single-responsibility services, dependency inversion
  via the `StoragePort` interface, and constructor-injected collaborators.
- **Design patterns** — Singleton (`BankConfig`), Factory (`AccountTypeFactory`),
  Builder (`BankAccountBuilder`), Observer (`BankEventObserver` and its
  observers), Strategy (`InterestStrategy`), Template Method (`MonthlyOperation`),
  and Command (`BankCommand` + `CommandHistory`).
- **Concurrency** — `synchronized` account operations, concurrent collections
  (`ConcurrentHashMap`, `CopyOnWriteArrayList`), lock-ordered transfers to
  prevent deadlock, and a scheduled auto-save service.
- **Unit testing** — behaviour-driven tests with JUnit 5 and mocked
  collaborators using Mockito.
