package org.example.service;

import org.example.model.BankAccount;
import org.example.model.Transaction;
import org.example.model.TransactionType;
import org.example.model.AccountStatus;

import java.util.*;
import java.util.function.Function;

public class GenericDemo {

    // ================================================
    // BOUNDED TYPE PARAMETER — restricting what T can be
    // ================================================

    // T must implement Comparable — guarantees we can compare items
    public static <T extends Comparable<T>> T findMax(List<T> list) {
        if (list.isEmpty()) throw new NoSuchElementException("List is empty");
        T max = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).compareTo(max) > 0) {
                max = list.get(i);
            }
        }
        return max;
    }

    public static <T extends Comparable<T>> T findMin(List<T> list) {
        if (list.isEmpty()) throw new NoSuchElementException("List is empty");
        T min = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).compareTo(min) < 0) {
                min = list.get(i);
            }
        }
        return min;
    }

    // Multiple bounds: T must be Comparable AND implement an interface
    // public static <T extends Comparable<T> & Serializable> void process(T item) { }

    // ================================================
    // WILDCARDS — ? extends T, ? super T
    // ================================================

    // ? extends Number → "any Number or subclass" (Integer, Double, etc.)
    // PRODUCER: you can READ from it, but can't WRITE to it
    public static double sumAll(List<? extends Number> numbers) {
        // This works with List<Integer>, List<Double>, List<Long>, etc.
        double total = 0;
        for (Number n : numbers) {
            total += n.doubleValue(); // ✅ can READ as Number
        }
        // numbers.add(42); // ❌ COMPILE ERROR — can't add (might be List<Double>)
        return total;
    }

    // ? super Integer → "any supertype of Integer" (Integer, Number, Object)
    // CONSUMER: you can WRITE to it, but reading gives you Object
    public static void fillWithNumbers(List<? super Integer> target, int count) {
        // This works with List<Integer>, List<Number>, List<Object>
        for (int i = 1; i <= count; i++) {
            target.add(i); // ✅ can ADD Integer (it IS-A Number, IS-A Object)
        }
        // Integer n = target.get(0); // ❌ can only read as Object
    }

    // ================================================
    // PECS: Producer Extends, Consumer Super
    // ================================================

    // Copy from source (PRODUCER — extends) to destination (CONSUMER — super)
    public static <T> void copy(List<? extends T> source,
                                List<? super T> destination) {
        for (T item : source) {       // READ from source (extends)
            destination.add(item);     // WRITE to destination (super)
        }
    }

    // ================================================
    // PRACTICAL DEMO
    // ================================================

    public static void runDemo(List<BankAccount> accounts) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║           GENERICS DEMO                  ║");
        System.out.println("╠══════════════════════════════════════════╣");

        // --- 1. Repository<BankAccount> ---
        System.out.println("║");
        System.out.println("║  1. Repository<BankAccount>:");

        Repository<BankAccount> accountRepo = new Repository<>("Accounts");
        accounts.forEach(accountRepo::add);

        accountRepo.printAll();

        // Find active accounts
        accountRepo.printMatching(BankAccount::isActive, "Active accounts");

        // Group by status using generic groupBy
        Map<AccountStatus, List<BankAccount>> byStatus =
                accountRepo.groupBy(BankAccount::getStatus);
        System.out.println("  Grouped by status:");
        byStatus.forEach((status, accs) ->
                System.out.println("    " + status.icon() + " " + status + ": " + accs.size()));

        // Sum balances using generic sumDouble
        double total = accountRepo.sumDouble(BankAccount::getBalance);
        System.out.printf("  Total balance: $%,.2f%n", total);

        // Top 2 by balance
        List<BankAccount> top2 = accountRepo.topN(2,
                Comparator.comparingDouble(BankAccount::getBalance).reversed());
        System.out.println("  Top 2 by balance:");
        top2.forEach(acc -> System.out.println("    " + acc));

        // --- 2. Repository<Transaction> — SAME class, DIFFERENT type ---
        System.out.println("║");
        System.out.println("║  2. Repository<Transaction> (same class, different type):");

        Repository<Transaction> txnRepo = new Repository<>("Transactions");
        accounts.stream()
                .flatMap(acc -> acc.getTransactions().stream())
                .forEach(txnRepo::add);

        System.out.println("  Total transactions: " + txnRepo.size());

        // Group by type using the SAME generic method
        Map<TransactionType, Long> txnCounts = txnRepo.countBy(Transaction::getType);
        System.out.println("  By type:");
        txnCounts.forEach((type, count) ->
                System.out.println("    " + type.displayName() + ": " + count));

        // Sum amounts
        double txnTotal = txnRepo.sumDouble(Transaction::getAmount);
        System.out.printf("  Total amount: $%,.2f%n", txnTotal);

        // --- 3. Repository<String> — works with ANY type ---
        System.out.println("║");
        System.out.println("║  3. Repository<String> (works with any type):");

        Repository<String> nameRepo = new Repository<>("Names");
        nameRepo.add("Alice");
        nameRepo.add("Bob");
        nameRepo.add("Carol");
        nameRepo.add("Alice"); // duplicate — Repository doesn't enforce uniqueness

        nameRepo.printAll();
        nameRepo.printMatching(name -> name.startsWith("A"), "Names starting with A");

        // --- 4. KeyValueStore<String, BankAccount> ---
        System.out.println("║");
        System.out.println("║  4. KeyValueStore<String, BankAccount>:");

        KeyValueStore<String, BankAccount> accLookup = new KeyValueStore<>("Account Lookup");
        accounts.forEach(acc -> accLookup.put(acc.getAccountNumber(), acc));

        // Optional-based lookup
        accLookup.get("ACC-0001")
                .ifPresentOrElse(
                        acc -> System.out.println("  Found: " + acc),
                        () -> System.out.println("  ACC-0001 not found"));

        accLookup.get("ACC-9999")
                .ifPresentOrElse(
                        acc -> System.out.println("  Found: " + acc),
                        () -> System.out.println("  ACC-9999 not found (expected)"));

        // Transform values to a different type
        KeyValueStore<String, String> nameLookup =
                accLookup.transformValues("Name Lookup", BankAccount::getOwnerName);
        nameLookup.printAll();

        // --- 5. Pair<String, Double> ---
        System.out.println("║");
        System.out.println("║  5. Pair<String, Double>:");

        List<Pair<String, Double>> rankings = accounts.stream()
                .sorted(Comparator.comparingDouble(BankAccount::getBalance).reversed())
                .map(acc -> Pair.of(acc.getOwnerName(), acc.getBalance()))
                .toList();

        System.out.println("  Balance rankings:");
        for (int i = 0; i < rankings.size(); i++) {
            Pair<String, Double> entry = rankings.get(i);
            System.out.printf("    %d. %s — $%,.2f%n",
                    i + 1, entry.first(), entry.second());
        }

        // Transform pair
        Pair<String, Double> first = rankings.get(0);
        Pair<String, String> formatted = first.mapSecond(
                bal -> String.format("$%,.2f", bal));
        System.out.println("  Richest (formatted): " + formatted);

        // --- 6. Bounded types ---
        System.out.println("║");
        System.out.println("║  6. Bounded types (<T extends Comparable>):");

        // Works with Integer (implements Comparable)
        List<Integer> numbers = List.of(42, 7, 99, 13, 56);
        System.out.println("  Max of " + numbers + ": " + findMax(numbers));
        System.out.println("  Min of " + numbers + ": " + findMin(numbers));

        // Works with String (implements Comparable)
        List<String> words = List.of("banana", "apple", "cherry");
        System.out.println("  Max of " + words + ": " + findMax(words));
        System.out.println("  Min of " + words + ": " + findMin(words));

        // Works with Transaction (we made it Comparable in Step 17)
        if (!txnRepo.isEmpty()) {
            Transaction maxTxn = findMax(txnRepo.getAll());
            System.out.println("  Max transaction (by ID): TXN-"
                    + String.format("%04d", maxTxn.getId()));
        }

        // --- 7. Wildcards (PECS) ---
        System.out.println("║");
        System.out.println("║  7. Wildcards (? extends, ? super):");

        // ? extends Number — accepts List<Integer>, List<Double>, etc.
        List<Integer> ints = List.of(1, 2, 3, 4, 5);
        List<Double> doubles = List.of(1.5, 2.5, 3.5);
        System.out.println("  Sum of ints:    " + sumAll(ints));
        System.out.println("  Sum of doubles: " + sumAll(doubles));
        // sumAll accepts BOTH because Integer and Double both extend Number

        // ? super Integer — write integers into List<Number>
        List<Number> numberList = new ArrayList<>();
        fillWithNumbers(numberList, 5);
        System.out.println("  Filled list: " + numberList);

        // copy from producer (extends) to consumer (super)
        List<Integer> source = List.of(10, 20, 30);
        List<Number> destination = new ArrayList<>();
        copy(source, destination);
        System.out.println("  Copied: " + destination);

        System.out.println("║");
        System.out.println("╚══════════════════════════════════════════╝");
    }

    // ================================================
    // TYPE ERASURE — what the JVM actually sees
    // ================================================

    public static void demonstrateTypeErasure() {
        System.out.println();
        System.out.println("--- Type Erasure Demo ---");

        List<String> strings = new ArrayList<>();
        List<Integer> integers = new ArrayList<>();

        // At runtime, both are just "ArrayList" — generics are ERASED
        System.out.println("strings.getClass():  " + strings.getClass().getName());
        System.out.println("integers.getClass(): " + integers.getClass().getName());
        System.out.println("Same class? " + (strings.getClass() == integers.getClass()));
        // true! Both are java.util.ArrayList at runtime

        System.out.println();
        System.out.println("CONSEQUENCES OF TYPE ERASURE:");
        System.out.println("  1. Cannot do: if (obj instanceof List<String>) — type gone");
        System.out.println("  2. Cannot do: new T() — JVM doesn't know what T is");
        System.out.println("  3. Cannot do: new T[10] — same reason");
        System.out.println("  4. List<String> and List<Integer> are same class at runtime");
        System.out.println();
        System.out.println("Generics are a COMPILE-TIME safety net.");
        System.out.println("They prevent bugs BEFORE your code runs.");
        System.out.println("At runtime, they disappear completely.");
    }
}