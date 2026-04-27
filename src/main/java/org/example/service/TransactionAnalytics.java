package org.example.service;

import org.example.model.BankAccount;
import org.example.model.Transaction;
import org.example.model.TransactionType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class TransactionAnalytics {

    // ================================================
    // TYPE 1: STATIC METHOD REFERENCE — ClassName::staticMethod
    // ================================================
    // The method is static — it belongs to the class, not an object.
    // Lambda:           x -> Math.abs(x)
    // Method reference: Math::abs

    // Our static helper methods (used as method references below)
    public static boolean isLargeTransaction(Transaction txn) {
        return txn.getAmount() > 1000;
    }

    public static boolean isTodayTransaction(Transaction txn) {
        return txn.isToday();
    }

    public static String formatForDisplay(Transaction txn) {
        return String.format("  %-12s %s  %s",
                txn.getType().displayName(),
                txn.getType().formatAmount(txn.getAmount()),
                txn.getTimeAgo());
    }

    public static String formatForCSV(Transaction txn) {
        return String.join(",",
                String.valueOf(txn.getId()),
                txn.getType().name(),
                String.valueOf(txn.getAmount()));
    }

    // Using static method references:
    public static void demonstrateStaticMethodRef(List<Transaction> txns) {
        System.out.println("--- TYPE 1: Static Method References ---");
        System.out.println();

        // METHOD REFERENCE version — same behavior, cleaner:
        List<Transaction> large = filter(txns, TransactionAnalytics::isLargeTransaction);
        //                                     ↑ ClassName::staticMethod
        System.out.println("Large transactions (>$1000): " + large.size());

        // LAMBDA: txns.forEach(t -> formatForDisplay(t));
        // METHOD REF:
        List<String> formatted = map(txns, TransactionAnalytics::formatForDisplay);
        formatted.forEach(System.out::println); // Type 2 reference (covered below)
    }

    // ================================================
    // TYPE 2: INSTANCE METHOD on a SPECIFIC OBJECT — object::method
    // ================================================
    // You have a specific object and want to call its method.
    // Lambda:           x -> System.out.println(x)
    // Method reference: System.out::println
    //                   ↑ 'System.out' is a specific PrintStream object

    public static void demonstrateInstanceOnObject(List<Transaction> txns,
                                                   BankAccount account) {
        System.out.println("--- TYPE 2: Instance Method on Specific Object ---");
        System.out.println();

        // System.out is a specific object — we call println on IT
        // LAMBDA:      txns.forEach(t -> System.out.println(t));
        // METHOD REF:
        System.out.println("All transactions:");
        txns.forEach(System.out::println);
        //           ↑ object::method — System.out is the object

        // StringBuilder is a specific object — we call append on IT
        StringBuilder report = new StringBuilder();
        // LAMBDA:      txns.forEach(t -> report.append(t.toString()).append("\n"));
        // Can't use method ref here — we're doing more than just calling one method
        // (we're calling append twice, and calling toString() first)
        // KEEP THE LAMBDA:
        txns.forEach(t -> report.append(t).append("\n"));
        System.out.println("Report built: " + report.length() + " chars");

        // account is a specific object — we call deposit on IT
        // LAMBDA:      amounts.forEach(a -> account.deposit(a));
        // METHOD REF:  amounts.forEach(account::deposit);
        // (we won't actually call this — just showing the concept)
        System.out.println("account::deposit would deposit into " + account.getOwnerName());
    }

    // ================================================
    // TYPE 3: INSTANCE METHOD on an ARBITRARY OBJECT — ClassName::instanceMethod
    // ================================================
    // The object to call the method on comes FROM the lambda parameter.
    // Lambda:           x -> x.getAmount()
    // Method reference: Transaction::getAmount
    //                   ↑ "call getAmount on whatever Transaction you receive"

    public static void demonstrateInstanceOnArbitrary(List<Transaction> txns) {
        System.out.println("--- TYPE 3: Instance Method on Arbitrary Object ---");
        System.out.println();

        // LAMBDA:      map(txns, t -> t.getAmount())
        // METHOD REF:
        List<Double> amounts = map(txns, Transaction::getAmount);
        //                               ↑ ClassName::instanceMethod
        //                               "call getAmount on each Transaction"
        System.out.println("Amounts: " + amounts);

        // LAMBDA:      map(txns, t -> t.getTypeName())
        // METHOD REF:
        List<String> types = map(txns, Transaction::getTypeName);
        System.out.println("Types: " + types);

        // Sorting with Type 3 reference:
        List<Transaction> sorted = new ArrayList<>(txns);

        // LAMBDA:      sorted.sort((a, b) -> a.compareTo(b));
        // METHOD REF:
        sorted.sort(Transaction::compareTo);
        //           ↑ "call compareTo on the first argument, passing the second"
        //           This works because compareTo takes one parameter of same type

        System.out.println("Sorted by natural order (ID):");
        sorted.forEach(t -> System.out.println("  TXN-" + String.format("%04d", t.getId())));

        // String example — easier to understand:
        List<String> names = List.of("Charlie", "alice", "Bob");
        List<String> upper = map(names, String::toUpperCase);
        //                             ↑ "call toUpperCase on each String"
        //                             each String IS the arbitrary object
        System.out.println("Uppercase names: " + upper);
    }

    // ================================================
    // TYPE 4: CONSTRUCTOR REFERENCE — ClassName::new
    // ================================================
    // Lambda:           () -> new ArrayList<>()
    // Method reference: ArrayList::new
    //                   ↑ "call the constructor"

    public static void demonstrateConstructorRef(List<Transaction> txns) {
        System.out.println("--- TYPE 4: Constructor Reference ---");
        System.out.println();

        // LAMBDA:      Supplier<ArrayList<Transaction>> factory = () -> new ArrayList<>();
        // METHOD REF:
        java.util.function.Supplier<ArrayList<Transaction>> factory = ArrayList::new;
        //                                                             ↑ ClassName::new

        ArrayList<Transaction> newList = factory.get(); // calls new ArrayList<>()
        System.out.println("Created empty list via constructor ref: size=" + newList.size());

        // Copy constructor:
        // LAMBDA:      Function<List<Transaction>, ArrayList<Transaction>> copier = list -> new ArrayList<>(list);
        // METHOD REF:
        java.util.function.Function<List<Transaction>, ArrayList<Transaction>> copier = ArrayList::new;
        //  ↑ This matches the constructor ArrayList(Collection c)

        ArrayList<Transaction> copy = copier.apply(txns);
        System.out.println("Copied " + copy.size() + " transactions via constructor ref");
    }

    // ================================================
    // PRACTICAL: Full analytics pipeline using method references
    // ================================================

    public static void runFullAnalytics(BankAccount account) {
        List<Transaction> txns = account.getTransactions();

        System.out.println();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║    Analytics for " + account.getOwnerName());
        System.out.println("╠══════════════════════════════════════════╣");

        if (txns.isEmpty()) {
            System.out.println("║  No transactions to analyze.");
            System.out.println("╚══════════════════════════════════════════╝");
            return;
        }

        // Extract all amounts using Type 3 method reference
        List<Double> amounts = map(txns, Transaction::getAmount);

        // Sum using static method reference
        double total = sum(txns, Transaction::getAmount);
        //                       ↑ Type 3: "call getAmount on each txn"

        // Count large transactions using static method reference
        long largeCount = count(txns, TransactionAnalytics::isLargeTransaction);
        //                            ↑ Type 1: "call this static method"

        // Get types using Type 3 method reference
        List<String> typeNames = map(txns, Transaction::getTypeName);

        // Find min and max
        Transaction biggest = findMax(txns, Comparator.comparingDouble(Transaction::getAmount));
        //                                                             ↑ Type 3 ref inside factory
        Transaction smallest = findMin(txns, Comparator.comparingDouble(Transaction::getAmount));

        // Display results
        System.out.println("║  Total transactions: " + txns.size());
        System.out.printf("║  Total amount:       $%,.2f%n", total);
        System.out.printf("║  Average amount:     $%,.2f%n", total / txns.size());
        System.out.println("║  Large (>$1000):     " + largeCount);

        if (biggest != null) {
            System.out.printf("║  Biggest:  %s %s%n",
                    biggest.getType().formatAmount(biggest.getAmount()),
                    biggest.getType().displayName());
        }
        if (smallest != null) {
            System.out.printf("║  Smallest: %s %s%n",
                    smallest.getType().formatAmount(smallest.getAmount()),
                    smallest.getType().displayName());
        }

        // Format and print each transaction using static method ref
        System.out.println("╠══════════════════════════════════════════╣");

        // LAMBDA:      map(txns, t -> formatForDisplay(t))
        // METHOD REF:
        List<String> lines = map(txns, TransactionAnalytics::formatForDisplay);
        //                             ↑ Type 1: static method reference
        lines.forEach(System.out::println);
        //            ↑ Type 2: instance method on specific object

        System.out.println("╚══════════════════════════════════════════╝");
    }

    // ================================================
    // GENERIC UTILITY METHODS — accept any function via method reference
    // ================================================

    // Filter: keep elements matching a predicate
    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T item : list) {
            if (predicate.test(item)) result.add(item);
        }
        return result;
    }

    // Map: transform each element
    public static <T, R> List<R> map(List<T> list, Function<T, R> mapper) {
        List<R> result = new ArrayList<>();
        for (T item : list) {
            result.add(mapper.apply(item));
        }
        return result;
    }

    // Sum: extract a double from each element and add them up
    public static <T> double sum(List<T> list, ToDoubleFunction<T> extractor) {
        double total = 0;
        for (T item : list) {
            total += extractor.applyAsDouble(item);
        }
        return total;
    }

    // Count: how many match the predicate
    public static <T> long count(List<T> list, Predicate<T> predicate) {
        long count = 0;
        for (T item : list) {
            if (predicate.test(item)) count++;
        }
        return count;
    }

    // Find max by comparator
    public static <T> T findMax(List<T> list, Comparator<T> comparator) {
        if (list.isEmpty()) return null;
        T max = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            if (comparator.compare(list.get(i), max) > 0) {
                max = list.get(i);
            }
        }
        return max;
    }

    // Find min by comparator
    public static <T> T findMin(List<T> list, Comparator<T> comparator) {
        if (list.isEmpty()) return null;
        T min = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            if (comparator.compare(list.get(i), min) < 0) {
                min = list.get(i);
            }
        }
        return min;
    }
}