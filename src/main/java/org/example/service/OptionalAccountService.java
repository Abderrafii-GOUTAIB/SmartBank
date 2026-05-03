package org.example.service;

import org.example.model.AccountStatus;
import org.example.model.BankAccount;
import org.example.model.Transaction;
import org.example.model.exception.AccountNotFoundException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

public class OptionalAccountService {

    // ================================================
    // CREATING OPTIONALS
    // ================================================

    // Optional.of(value) — value MUST NOT be null (throws if null)
    // Optional.ofNullable(value) — value CAN be null (becomes empty Optional)
    // Optional.empty() — explicitly empty

    // ================================================
    // LOOKUP — return Optional instead of null
    // ================================================

    // BEFORE (returns null):
    //   public BankAccount findByAccountNumberOrNull(String accNum) {
    //       return accountMap.get(accNum);  // null if not found
    //   }
    //
    // AFTER (returns Optional):
    public static Optional<BankAccount> findByAccountNumber(
            List<BankAccount> accounts, String accountNumber) {
        return accounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(accountNumber))
                .findFirst();
        // findFirst() already returns Optional<BankAccount>!
        // No need to manually wrap — streams do it for us
    }

    // Search by name — might find zero, one, or many
    // For "find one", Optional is perfect
    public static Optional<BankAccount> findByName(
            List<BankAccount> accounts, String name) {
        return accounts.stream()
                .filter(acc -> acc.getOwnerName().equalsIgnoreCase(name))
                .findFirst();
    }

    // Search by email
    public static Optional<BankAccount> findByEmail(
            List<BankAccount> accounts, String email) {
        return accounts.stream()
                .filter(acc -> acc.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    // Find the richest account
    public static Optional<BankAccount> findRichest(List<BankAccount> accounts) {
        return accounts.stream()
                .max(Comparator.comparingDouble(BankAccount::getBalance));
    }

    // Find the richest ACTIVE account
    public static Optional<BankAccount> findRichestActive(List<BankAccount> accounts) {
        return accounts.stream()
                .filter(BankAccount::isActive)
                .max(Comparator.comparingDouble(BankAccount::getBalance));
    }

    // Find the largest transaction for an account
    public static Optional<Transaction> findLargestTransaction(BankAccount account) {
        return account.getTransactions().stream()
                .max(Comparator.comparingDouble(Transaction::getAmount));
    }

    // Find most recent transaction
    public static Optional<Transaction> findMostRecentTransaction(BankAccount account) {
        return account.getTransactions().stream()
                .max(Comparator.comparing(Transaction::getDateTime));
    }

    // ================================================
    // CONSUMING OPTIONALS — the right way
    // ================================================

    public static void demonstrateConsuming(List<BankAccount> accounts) {
        System.out.println("--- Optional: Consuming Values ---");
        System.out.println();

        // ─── 1. ifPresent() — do something ONLY if value exists ───

        System.out.println("1. ifPresent():");
        Optional<BankAccount> richest = findRichest(accounts);

        // BEFORE (null check):
        // BankAccount r = findRichestOrNull(accounts);
        // if (r != null) { System.out.println(r); }

        // AFTER (Optional):
        richest.ifPresent(acc ->
                System.out.println("   Richest: " + acc.getOwnerName()
                        + " $" + String.format("%,.2f", acc.getBalance())));

        // ─── 2. ifPresentOrElse() — handle BOTH cases ───

        System.out.println("\n2. ifPresentOrElse():");
        findByAccountNumber(accounts, "ACC-9999")
                .ifPresentOrElse(
                        acc -> System.out.println("   Found: " + acc),
                        () -> System.out.println("   ACC-9999 not found — handled gracefully!")
                );

        // ─── 3. orElse() — provide a DEFAULT value ───

        System.out.println("\n3. orElse():");
        BankAccount defaultAcc = new BankAccount("Default User", "default@bank.com", 0);
        BankAccount result = findByAccountNumber(accounts, "ACC-9999")
                .orElse(defaultAcc);
        System.out.println("   Result: " + result);
        // If ACC-9999 exists → returns it. Otherwise → returns defaultAcc

        // ─── 4. orElseGet() — lazy default (computed only if needed) ───

        System.out.println("\n4. orElseGet() — lazy:");
        BankAccount lazyResult = findByAccountNumber(accounts, "ACC-9999")
                .orElseGet(() -> {
                    System.out.println("   (Computing default — this only runs if empty)");
                    return new BankAccount("Lazy Default", "lazy@bank.com", 0);
                });
        System.out.println("   Result: " + lazyResult);

        // orElse vs orElseGet:
        //   orElse(defaultAcc)     → defaultAcc is ALWAYS created, even if not needed
        //   orElseGet(() -> ...)   → supplier runs ONLY when Optional is empty
        // Use orElseGet when creating the default is expensive

        // ─── 5. orElseThrow() — throw if empty ───

        System.out.println("\n5. orElseThrow():");
        try {
            BankAccount mustExist = findByAccountNumber(accounts, "ACC-0001")
                    .orElseThrow(() ->
                            new AccountNotFoundException("ACC-0001"));
            System.out.println("   Found: " + mustExist);
        } catch (AccountNotFoundException e) {
            System.out.println("   " + e.toDisplayString());
        }
    }

    // ================================================
    // TRANSFORMING OPTIONALS — map, flatMap, filter
    // ================================================

    public static void demonstrateTransforming(List<BankAccount> accounts) {
        System.out.println("--- Optional: Transforming Values ---");
        System.out.println();

        // ─── 1. map() — transform the value inside Optional ───

        System.out.println("1. map() — extract nested data safely:");

        // BEFORE (null checks everywhere):
        // BankAccount acc = findByNumber("ACC-0001");
        // if (acc != null) {
        //     String name = acc.getOwnerName();
        //     if (name != null) {
        //         String upper = name.toUpperCase();
        //         System.out.println(upper);
        //     }
        // }

        // AFTER (Optional chaining):
        String ownerName = findByAccountNumber(accounts, "ACC-0001")
                .map(BankAccount::getOwnerName)       // Optional<BankAccount> → Optional<String>
                .map(String::toUpperCase)             // Optional<String> → Optional<String>
                .orElse("Unknown");                   // unwrap with default
        System.out.println("   ACC-0001 owner (upper): " + ownerName);

        // If ACC-0001 doesn't exist → map is NEVER called → "Unknown"
        // No null check needed at any step!

        // ─── Chain: account → balance → formatted ───

        String balanceStr = findByAccountNumber(accounts, "ACC-0001")
                .map(BankAccount::getBalance)
                .map(bal -> String.format("$%,.2f", bal))
                .orElse("N/A");
        System.out.println("   ACC-0001 balance: " + balanceStr);

        // ─── Chain: account → status → icon ───

        String statusIcon = findByAccountNumber(accounts, "ACC-0001")
                .map(BankAccount::getStatus)
                .map(AccountStatus::icon)
                .orElse("?");
        System.out.println("   ACC-0001 status: " + statusIcon);

        // Missing account — entire chain safely returns default:
        String missing = findByAccountNumber(accounts, "ACC-9999")
                .map(BankAccount::getOwnerName)
                .map(String::toUpperCase)
                .orElse("NOT FOUND");
        System.out.println("   ACC-9999 owner: " + missing);

        // ─── 2. filter() — add conditions to Optional ───

        System.out.println("\n2. filter() — conditional unwrapping:");

        // "Give me ACC-0001, but ONLY if it's active"
        Optional<BankAccount> activeOnly = findByAccountNumber(accounts, "ACC-0001")
                .filter(BankAccount::isActive);
        //  If account exists AND is active → present
        //  If account exists but NOT active → empty
        //  If account doesn't exist → empty

        activeOnly.ifPresentOrElse(
                acc -> System.out.println("   ACC-0001 is active: " + acc),
                () -> System.out.println("   ACC-0001 is not active or doesn't exist"));

        // "Give me the richest account, but only if balance > $5000"
        findRichest(accounts)
                .filter(acc -> acc.getBalance() > 5000)
                .ifPresentOrElse(
                        acc -> System.out.println("   Rich account found: " + acc),
                        () -> System.out.println("   No account with balance > $5000"));

        // ─── 3. flatMap() — when the transformation itself returns Optional ───

        System.out.println("\n3. flatMap() — avoiding Optional<Optional<T>>:");

        // findLargestTransaction returns Optional<Transaction>
        // Without flatMap: map would give Optional<Optional<Transaction>> — nested!
        // With flatMap: we get Optional<Transaction> — flat!

        Optional<String> largestTxnDesc = findByAccountNumber(accounts, "ACC-0001")
                .flatMap(OptionalAccountService::findLargestTransaction)
                // ↑ flatMap because findLargestTransaction returns Optional
                //   map would give: Optional<Optional<Transaction>>
                //   flatMap gives:  Optional<Transaction>
                .map(txn -> txn.getType().formatAmount(txn.getAmount())
                        + " " + txn.getType().displayName());

        System.out.println("   ACC-0001's largest txn: " + largestTxnDesc.orElse("No transactions"));

        // Full chain: find account → find largest txn → format amount
        String fullChain = findByAccountNumber(accounts, "ACC-0001")
                .flatMap(OptionalAccountService::findLargestTransaction)
                .map(Transaction::getAmount)
                .map(amt -> String.format("$%,.2f", amt))
                .orElse("N/A");
        System.out.println("   Largest txn amount: " + fullChain);
    }

    // ================================================
    // OPTIONAL WITH STREAMS — they work together naturally
    // ================================================

    public static void demonstrateWithStreams(List<BankAccount> accounts) {
        System.out.println("--- Optional + Streams ---");
        System.out.println();

        // Stream operations that return Optional:
        //   findFirst(), findAny(), min(), max(), reduce()

        // ─── findFirst with chaining ───
        String firstActiveName = accounts.stream()
                .filter(BankAccount::isActive)
                .findFirst()                           // Optional<BankAccount>
                .map(BankAccount::getOwnerName)        // Optional<String>
                .orElse("No active accounts");         // String
        System.out.println("First active: " + firstActiveName);

        // ─── max with chaining ───
        String richestInfo = accounts.stream()
                .max(Comparator.comparingDouble(BankAccount::getBalance))
                .map(acc -> acc.getOwnerName() + " ($" + String.format("%,.2f", acc.getBalance()) + ")")
                .orElse("No accounts");
        System.out.println("Richest: " + richestInfo);

        // ─── OptionalDouble from numeric streams ───
        OptionalDouble avgBalance = accounts.stream()
                .mapToDouble(BankAccount::getBalance)
                .average();
        // OptionalDouble has its own ifPresent, orElse, etc.
        avgBalance.ifPresent(avg ->
                System.out.printf("Average balance: $%,.2f%n", avg));

        // ─── or() — try alternative if first is empty (Java 9+) ───
        System.out.println();
        System.out.println("or() — fallback chain:");

        // "Find ACC-9999, or if not found, try ACC-8888, or if not found, try ACC-0001"
        Optional<BankAccount> fallback = findByAccountNumber(accounts, "ACC-9999")
                .or(() -> findByAccountNumber(accounts, "ACC-8888"))
                .or(() -> findByAccountNumber(accounts, "ACC-0001"));

        fallback.ifPresent(acc -> System.out.println("  Found via fallback: " + acc));

        // ─── stream() — convert Optional to Stream (Java 9+) ───
        // Useful when you have a list of Optionals and want to keep only present ones
        System.out.println();
        System.out.println("Optional.stream() — filter empty optionals:");

        List<String> accountNumbers = List.of("ACC-0001", "ACC-9999", "ACC-0002", "ACC-8888");

        List<BankAccount> found = accountNumbers.stream()
                .map(num -> findByAccountNumber(accounts, num))  // Stream<Optional<BankAccount>>
                .flatMap(Optional::stream)                        // Stream<BankAccount> (empties removed!)
                .toList();

        System.out.println("  Searched for " + accountNumbers.size()
                + " accounts, found " + found.size() + ":");
        found.forEach(acc -> System.out.println("    " + acc));
    }

    // ================================================
    // 🆕 PRACTICAL: Safe transaction report
    // ================================================

    public static void printSafeReport(List<BankAccount> accounts, String accountNumber) {
        System.out.println();
        System.out.println("--- Safe Report (Optional everywhere) ---");

        findByAccountNumber(accounts, accountNumber)
                .ifPresentOrElse(acc -> {
                    System.out.println("Account: " + acc);
                    System.out.println("Status:  " + acc.getStatus().icon() + " "
                            + acc.getStatus().description());

                    // Largest transaction (might not exist)
                    findLargestTransaction(acc)
                            .ifPresentOrElse(
                                    txn -> System.out.println("Largest:  "
                                            + txn.getType().formatAmount(txn.getAmount())
                                            + " " + txn.getType().displayName()),
                                    () -> System.out.println("Largest:  No transactions yet")
                            );

                    // Most recent transaction (might not exist)
                    findMostRecentTransaction(acc)
                            .ifPresentOrElse(
                                    txn -> System.out.println("Latest:   "
                                            + txn.getShortDateTime() + " "
                                            + txn.getType().formatAmount(txn.getAmount())),
                                    () -> System.out.println("Latest:   No transactions yet")
                            );

                    // Average transaction amount
                    OptionalDouble avg = acc.getTransactions().stream()
                            .mapToDouble(Transaction::getAmount)
                            .average();
                    avg.ifPresentOrElse(
                            a -> System.out.printf("Avg txn:  $%,.2f%n", a),
                            () -> System.out.println("Avg txn:  N/A")
                    );

                }, () -> System.out.println("Account " + accountNumber + " not found."));
    }

    // ================================================
    // ❌ ANTI-PATTERNS — what NOT to do with Optional
    // ================================================

    public static void demonstrateAntiPatterns() {
        System.out.println("--- Optional: Anti-Patterns (DON'T DO THESE) ---");
        System.out.println();

        // ❌ ANTI-PATTERN 1: isPresent() + get() — defeats the purpose!
        // Optional<BankAccount> opt = findRichest(accounts);
        // if (opt.isPresent()) {
        //     BankAccount acc = opt.get();  // this is just a null check with extra steps!
        // }
        // ✅ Instead: opt.ifPresent(acc -> ...)
        System.out.println("1. ❌ isPresent() + get() = null check in disguise");
        System.out.println("   ✅ Use ifPresent(), map(), orElse() instead");

        // ❌ ANTI-PATTERN 2: Optional as method parameter
        // void deposit(Optional<Double> amount) { ... }
        // ✅ Instead: overload the method or use a default value
        System.out.println("2. ❌ Optional as parameter = confusing API");
        System.out.println("   ✅ Overload the method: deposit(double) and deposit()");

        // ❌ ANTI-PATTERN 3: Optional as class field
        // class Account { Optional<String> middleName; }
        // ✅ Instead: just use null for fields, document it
        System.out.println("3. ❌ Optional as field = unnecessary overhead");
        System.out.println("   ✅ Use null for fields, Optional for return types");

        // ❌ ANTI-PATTERN 4: Optional.of() with potentially null value
        // String name = null;
        // Optional<String> opt = Optional.of(name);  // 💥 NullPointerException!
        // ✅ Instead: Optional.ofNullable(name);
        System.out.println("4. ❌ Optional.of(possiblyNull) → crashes!");
        System.out.println("   ✅ Use Optional.ofNullable() for uncertain values");

        // ❌ ANTI-PATTERN 5: Returning Optional of empty collection
        // Optional<List<Transaction>> getTxns() → Optional.of(emptyList)
        // ✅ Instead: just return the empty list directly
        System.out.println("5. ❌ Optional<List<T>> — just return empty list");
        System.out.println("   ✅ Collections already represent emptiness");
    }
}