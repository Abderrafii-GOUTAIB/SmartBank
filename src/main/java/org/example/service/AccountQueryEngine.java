package org.example.service;

import org.example.model.AccountStatus;
import org.example.model.BankAccount;
import org.example.model.Transaction;
import org.example.model.TransactionType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class AccountQueryEngine {

    // ================================================
    // 1. PREDICATE<T> — T → boolean — "Is this true?"
    // ================================================
    // Replaces our custom TransactionFilter interface!
    // Built-in methods: .and(), .or(), .negate() — free composition!

    // --- Account predicates ---

    public static final Predicate<BankAccount> IS_ACTIVE =
            acc -> acc.getStatus() == AccountStatus.ACTIVE;

    public static final Predicate<BankAccount> IS_FROZEN =
            acc -> acc.getStatus() == AccountStatus.FROZEN;

    public static final Predicate<BankAccount> HAS_TRANSACTIONS =
            acc -> acc.getTransactionCount() > 0;

    // Factory: create a predicate with a parameter
    public static Predicate<BankAccount> balanceAbove(double min) {
        return acc -> acc.getBalance() > min;
    }

    public static Predicate<BankAccount> balanceBelow(double max) {
        return acc -> acc.getBalance() < max;
    }

    public static Predicate<BankAccount> balanceBetween(double min, double max) {
        // 🆕 BUILT-IN .and() — no need for our custom TransactionProcessor.and()!
        return balanceAbove(min).and(balanceBelow(max));
    }

    public static Predicate<BankAccount> nameContains(String text) {
        return acc -> acc.getOwnerName().toLowerCase().contains(text.toLowerCase());
    }

    // --- Transaction predicates ---

    public static final Predicate<Transaction> IS_DEPOSIT =
            txn -> txn.getType() == TransactionType.DEPOSIT;

    public static final Predicate<Transaction> IS_WITHDRAWAL =
            txn -> txn.getType() == TransactionType.WITHDRAWAL;

    public static final Predicate<Transaction> IS_CREDIT =
            txn -> txn.getType().isCredit();

    public static final Predicate<Transaction> IS_TODAY =
            Transaction::isToday;  // method reference!

    public static Predicate<Transaction> amountAbove(double min) {
        return txn -> txn.getAmount() > min;
    }

    // ================================================
    // 🆕 PREDICATE COMPOSITION — the power of built-in interfaces
    // ================================================

    public static void demonstratePredicate(List<BankAccount> accounts) {
        System.out.println("--- PREDICATE<T> Demo ---");
        System.out.println();

        // Single predicate
        System.out.println("Active accounts:");
        filter(accounts, IS_ACTIVE).forEach(a -> System.out.println("  " + a));

        // .and() — both must be true
        Predicate<BankAccount> activeAndRich = IS_ACTIVE.and(balanceAbove(1000));
        System.out.println("\nActive AND balance > $1000:");
        filter(accounts, activeAndRich).forEach(a -> System.out.println("  " + a));

        // .or() — either can be true
        Predicate<BankAccount> frozenOrPoor = IS_FROZEN.or(balanceBelow(100));
        System.out.println("\nFrozen OR balance < $100:");
        filter(accounts, frozenOrPoor).forEach(a -> System.out.println("  " + a));

        // .negate() — flip the result
        Predicate<BankAccount> notActive = IS_ACTIVE.negate();
        System.out.println("\nNOT active:");
        filter(accounts, notActive).forEach(a -> System.out.println("  " + a));

        // Complex chain: active AND (rich OR has transactions)
        Predicate<BankAccount> complex = IS_ACTIVE
                .and(balanceAbove(500).or(HAS_TRANSACTIONS));
        System.out.println("\nActive AND (balance > $500 OR has transactions):");
        filter(accounts, complex).forEach(a -> System.out.println("  " + a));
    }

    // ================================================
    // 2. FUNCTION<T, R> — T → R — "Transform this"
    // ================================================
    // Takes one type, returns another type.
    // Built-in methods: .andThen(), .compose() — pipeline building!

    // --- Account functions (extractors) ---

    public static final Function<BankAccount, String> GET_NAME =
            BankAccount::getOwnerName;  // method reference — Type 3

    public static final Function<BankAccount, String> GET_ACCOUNT_NUM =
            BankAccount::getAccountNumber;

    public static final Function<BankAccount, Double> GET_BALANCE =
            BankAccount::getBalance;

    public static final Function<BankAccount, String> GET_STATUS_DISPLAY =
            acc -> acc.getStatus().icon() + " " + acc.getStatus();

    // --- Formatting functions ---

    public static final Function<BankAccount, String> FORMAT_SIMPLE =
            acc -> String.format("[%s] %s — $%,.2f",
                    acc.getAccountNumber(), acc.getOwnerName(), acc.getBalance());

    public static final Function<BankAccount, String> FORMAT_DETAILED =
            acc -> String.format("[%s] %s | $%,.2f | %s | %d txns",
                    acc.getAccountNumber(),
                    acc.getOwnerName(),
                    acc.getBalance(),
                    acc.getStatus().icon() + " " + acc.getStatus(),
                    acc.getTransactionCount());

    // --- String functions for demonstration ---

    public static final Function<String, String> TO_UPPER = String::toUpperCase;
    public static final Function<String, String> TRIM = String::trim;
    public static final Function<String, Integer> STRING_LENGTH = String::length;

    // 🆕 FUNCTION COMPOSITION — .andThen() and .compose()
    public static void demonstrateFunction(List<BankAccount> accounts) {
        System.out.println("--- FUNCTION<T, R> Demo ---");
        System.out.println();

        // Simple extraction
        System.out.println("Account names:");
        map(accounts, GET_NAME).forEach(n -> System.out.println("  " + n));

        // .andThen() — apply THIS function, THEN the next one
        // getName → toUpperCase
        Function<BankAccount, String> getUpperName = GET_NAME.andThen(TO_UPPER);
        //                                                    ↑ first getName, THEN toUpperCase
        System.out.println("\nUppercase names (andThen):");
        map(accounts, getUpperName).forEach(n -> System.out.println("  " + n));

        // Chaining multiple functions: getName → trim → toUpperCase → length
        Function<BankAccount, Integer> nameToLength =
                GET_NAME                    // BankAccount → String
                        .andThen(TRIM)      // String → String
                        .andThen(TO_UPPER)  // String → String
                        .andThen(STRING_LENGTH); // String → Integer
        System.out.println("\nName lengths (chained pipeline):");
        for (BankAccount acc : accounts) {
            System.out.println("  " + acc.getOwnerName() + " → " + nameToLength.apply(acc) + " chars");
        }

        // .compose() — apply the OTHER function FIRST, then this one
        // compose is the reverse of andThen:
        //   f.andThen(g)  = first f, then g   → g(f(x))
        //   f.compose(g)  = first g, then f   → f(g(x))

        // FORMAT_DETAILED applied to each account
        System.out.println("\nDetailed format:");
        map(accounts, FORMAT_DETAILED).forEach(s -> System.out.println("  " + s));
    }

    // ================================================
    // 3. CONSUMER<T> — T → void — "Do something with this"
    // ================================================
    // Takes an input, returns nothing. Used for side effects.
    // Built-in method: .andThen() — chain multiple actions!

    public static final Consumer<BankAccount> PRINT_SIMPLE =
            acc -> System.out.println("  " + acc);

    public static final Consumer<BankAccount> PRINT_BALANCE =
            acc -> System.out.printf("  %s: $%,.2f%n", acc.getOwnerName(), acc.getBalance());

    public static final Consumer<BankAccount> PRINT_STATUS =
            acc -> System.out.println("  " + acc.getOwnerName() + ": "
                    + acc.getStatus().icon() + " " + acc.getStatus());

    // 🆕 CONSUMER COMPOSITION — .andThen() chains side effects
    public static void demonstrateConsumer(List<BankAccount> accounts) {
        System.out.println("--- CONSUMER<T> Demo ---");
        System.out.println();

        // Single consumer
        System.out.println("Simple print:");
        accounts.forEach(PRINT_SIMPLE);

        // .andThen() — do THIS, then THAT
        Consumer<BankAccount> fullPrint = PRINT_BALANCE.andThen(PRINT_STATUS);
        //                                              ↑ print balance AND THEN print status
        System.out.println("\nBalance + Status (andThen):");
        accounts.forEach(fullPrint);

        // Chain 3 consumers: print name, print balance, print separator
        Consumer<BankAccount> printName =
                acc -> System.out.println("  Name: " + acc.getOwnerName());
        Consumer<BankAccount> printBal =
                acc -> System.out.printf("  Balance: $%,.2f%n", acc.getBalance());
        Consumer<BankAccount> printSep =
                acc -> System.out.println("  ──────────────────");

        Consumer<BankAccount> fullReport = printName.andThen(printBal).andThen(printSep);
        System.out.println("\nChained consumer (3 actions per account):");
        accounts.forEach(fullReport);
    }

    // ================================================
    // 4. SUPPLIER<T> — () → T — "Give me something"
    // ================================================
    // Takes nothing, returns something. Used for factories, lazy eval.

    public static void demonstrateSupplier() {
        System.out.println("--- SUPPLIER<T> Demo ---");
        System.out.println();

        // Factory supplier — creates new objects
        Supplier<List<Transaction>> listFactory = ArrayList::new;
        //                                        ↑ constructor reference!

        List<Transaction> newList = listFactory.get(); // calls new ArrayList<>()
        System.out.println("Created list via Supplier: size=" + newList.size());

        // Lazy evaluation — the expensive operation only runs when .get() is called
        Supplier<String> lazyMessage = () -> {
            System.out.println("  (Computing expensive message...)");
            return "Bank report generated at " + java.time.LocalDateTime.now();
        };

        System.out.println("Supplier created — nothing computed yet.");
        System.out.println("Now calling .get():");
        String message = lazyMessage.get(); // NOW it runs
        System.out.println("Result: " + message);

        // Default value supplier
        Supplier<BankAccount> defaultAccount =
                () -> new BankAccount("Default User", "default@bank.com", 0);

        BankAccount def = defaultAccount.get();
        System.out.println("Default account: " + def);
    }

    // ================================================
    // 5. UNARYOPERATOR<T> — T → T — "Transform in place"
    // ================================================
    // Special case of Function<T, T> — input and output same type.

    public static void demonstrateUnaryOperator() {
        System.out.println("--- UNARYOPERATOR<T> Demo ---");
        System.out.println();

        // String transformations
        UnaryOperator<String> shout = s -> s.toUpperCase() + "!";
        UnaryOperator<String> trim = String::trim;
        UnaryOperator<String> addBrackets = s -> "[" + s + "]";

        // Chain transformations: trim → uppercase! → [brackets]
        UnaryOperator<String> pipeline = s -> addBrackets.apply(shout.apply(trim.apply(s)));

        List<String> names = List.of("  alice  ", "  bob  ", "  carol  ");
        System.out.println("Original → Transformed:");
        for (String name : names) {
            System.out.println("  \"" + name + "\" → " + pipeline.apply(name));
        }

        // replaceAll uses UnaryOperator — transforms each element in-place
        List<String> mutableNames = new ArrayList<>(names);
        mutableNames.replaceAll((UnaryOperator<String>) trim.andThen(shout));
        //                      ↑ UnaryOperator chaining with andThen
        System.out.println("\nAfter replaceAll: " + mutableNames);
    }

    // ================================================
    // 6. BINARYOPERATOR<T> — (T, T) → T — "Combine two"
    // ================================================
    // Special case of BiFunction<T, T, T> — two inputs and output same type.

    public static void demonstrateBinaryOperator(List<BankAccount> accounts) {
        System.out.println("--- BINARYOPERATOR<T> Demo ---");
        System.out.println();

        // Combine two balances
        BinaryOperator<Double> addBalances = Double::sum;
        //                                   ↑ static method reference

        // Reduce: combine all balances into one total
        double total = 0.0;
        for (BankAccount acc : accounts) {
            total = addBalances.apply(total, acc.getBalance());
        }
        System.out.printf("Total balance (reduce): $%,.2f%n", total);

        // Find the richer of two accounts
        BinaryOperator<BankAccount> richer =
                (a, b) -> a.getBalance() >= b.getBalance() ? a : b;

        // Reduce: find the richest account
        if (!accounts.isEmpty()) {
            BankAccount richest = accounts.get(0);
            for (int i = 1; i < accounts.size(); i++) {
                richest = richer.apply(richest, accounts.get(i));
            }
            System.out.println("Richest: " + richest);
        }

        // BinaryOperator with minBy/maxBy factories
        BinaryOperator<BankAccount> poorest =
                BinaryOperator.minBy(Comparator.comparingDouble(BankAccount::getBalance));
        if (!accounts.isEmpty()) {
            BankAccount min = accounts.get(0);
            for (int i = 1; i < accounts.size(); i++) {
                min = poorest.apply(min, accounts.get(i));
            }
            System.out.println("Poorest: " + min);
        }
    }

    // ================================================
    // 7. BIFUNCTION, BIPREDICATE, BICONSUMER — two-argument versions
    // ================================================

    public static void demonstrateBiInterfaces(List<BankAccount> accounts) {
        System.out.println("--- BI-INTERFACES Demo ---");
        System.out.println();

        // BiPredicate<T, U> — (T, U) → boolean
        java.util.function.BiPredicate<BankAccount, Double> hasMoreThan =
                (acc, min) -> acc.getBalance() > min;

        System.out.println("Accounts with > $500:");
        for (BankAccount acc : accounts) {
            if (hasMoreThan.test(acc, 500.0)) {
                System.out.println("  " + acc);
            }
        }

        // BiFunction<T, U, R> — (T, U) → R
        BiFunction<String, Double, String> formatEntry =
                (name, balance) -> String.format("  %-20s $%,.2f", name, balance);

        System.out.println("\nFormatted entries:");
        for (BankAccount acc : accounts) {
            System.out.println(formatEntry.apply(acc.getOwnerName(), acc.getBalance()));
        }

        // BiConsumer<T, U> — (T, U) → void
        BiConsumer<String, Double> printEntry =
                (name, bal) -> System.out.printf("  %s → $%,.2f%n", name, bal);

        System.out.println("\nBiConsumer output:");
        for (BankAccount acc : accounts) {
            printEntry.accept(acc.getOwnerName(), acc.getBalance());
        }
    }

    // ================================================
    // 🆕 PRACTICAL: Build a query pipeline
    // ================================================

    public static void runQuery(List<BankAccount> accounts,
                                Predicate<BankAccount> filter,
                                Function<BankAccount, String> formatter,
                                Consumer<String> output) {
        // This method is completely generic:
        // - ANY filter (Predicate)
        // - ANY formatter (Function)
        // - ANY output (Consumer)
        // Zero coupling to specific implementations!

        for (BankAccount acc : accounts) {
            if (filter.test(acc)) {
                String formatted = formatter.apply(acc);
                output.accept(formatted);
            }
        }
    }

    public static void demonstrateQueryPipeline(List<BankAccount> accounts) {
        System.out.println("--- Query Pipeline Demo ---");
        System.out.println();

        // Query 1: Active accounts with balance > $500, simple format, print to console
        System.out.println("Query: Active + balance > $500 (simple):");
        runQuery(accounts,
                IS_ACTIVE.and(balanceAbove(500)),     // Predicate
                FORMAT_SIMPLE,                         // Function
                System.out::println);                  // Consumer

        // Query 2: All accounts, detailed format, print to console
        System.out.println("\nQuery: All accounts (detailed):");
        runQuery(accounts,
                acc -> true,                           // accept all
                FORMAT_DETAILED,                       // Function
                s -> System.out.println("  >> " + s)); // custom Consumer

        // Query 3: Collect results instead of printing
        System.out.println("\nQuery: Frozen accounts (collected to list):");
        List<String> results = new ArrayList<>();
        runQuery(accounts,
                IS_FROZEN,                             // Predicate
                FORMAT_SIMPLE,                         // Function
                results::add);                         // Consumer — add to list!
        //       ↑ method reference: results.add(s)

        if (results.isEmpty()) {
            System.out.println("  No frozen accounts.");
        } else {
            results.forEach(s -> System.out.println("  " + s));
        }
    }

    // ================================================
    // UTILITY METHODS — generic, reusable
    // ================================================

    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T item : list) {
            if (predicate.test(item)) result.add(item);
        }
        return result;
    }

    public static <T, R> List<R> map(List<T> list, Function<T, R> mapper) {
        List<R> result = new ArrayList<>();
        for (T item : list) {
            result.add(mapper.apply(item));
        }
        return result;
    }
}