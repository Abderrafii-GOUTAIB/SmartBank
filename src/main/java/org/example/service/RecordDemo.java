package org.example.service;

import org.example.model.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecordDemo {

    public static void runDemo(List<BankAccount> accounts) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║              RECORDS DEMO                ║");
        System.out.println("╠══════════════════════════════════════════╣");

        // ================================================
        // 1. AccountSummary — snapshot record
        // ================================================
        System.out.println("║");
        System.out.println("║  1. AccountSummary (snapshot records):");

        // Create snapshots from live accounts
        List<AccountSummary> summaries = accounts.stream()
                .map(AccountSummary::from)     // method reference to static factory
                .toList();

        // Display — records have auto-generated toString()
        summaries.forEach(s -> System.out.println("║    " + s.toDisplayLine()));

        // Records work perfectly with streams (equals/hashCode for free)
        System.out.println("║");
        System.out.println("║  Sorted by balance (descending):");
        summaries.stream()
                .sorted(Comparator.comparingDouble(AccountSummary::balance).reversed())
                //                                  ↑ accessor is balance(), not getBalance()
                .forEach(s -> System.out.println("║    " + s.toDisplayLine()));

        // Group summaries by status
        Map<AccountStatus, List<AccountSummary>> byStatus = summaries.stream()
                .collect(Collectors.groupingBy(AccountSummary::status));

        System.out.println("║");
        System.out.println("║  Grouped by status:");
        byStatus.forEach((status, accs) ->
                System.out.println("║    " + status.icon() + " " + status
                        + ": " + accs.size() + " account(s)"));

        // ================================================
        // 2. TransferResult — operation result record
        // ================================================
        System.out.println("║");
        System.out.println("║  2. TransferResult (operation result records):");

        // Simulate some transfers
        TransferResult success1 = TransferResult.success(
                "ACC-0001", "ACC-0002", 500, 2000, 1300);
        TransferResult success2 = TransferResult.success(
                "ACC-0002", "ACC-0003", 200, 1100, 550);
        TransferResult failure1 = TransferResult.failure(
                "ACC-0003", "ACC-0001", 99999, "Insufficient funds");

        List<TransferResult> transfers = List.of(success1, success2, failure1);

        transfers.forEach(t -> System.out.println("║    " + t.toDisplayString()));

        // Filter successful transfers
        long successCount = transfers.stream()
                .filter(TransferResult::success)  // accessor: success() returns boolean
                .count();
        System.out.println("║    Successful: " + successCount + "/" + transfers.size());

        // Total transferred
        double totalTransferred = transfers.stream()
                .filter(TransferResult::success)
                .mapToDouble(TransferResult::amount)
                .sum();
        System.out.printf("║    Total transferred: $%,.2f%n", totalTransferred);

        // ================================================
        // 3. MoneyAmount — value object record
        // ================================================
        System.out.println("║");
        System.out.println("║  3. MoneyAmount (value object record):");

        MoneyAmount price = new MoneyAmount(29.99);
        MoneyAmount tax = price.multiply(0.2);
        MoneyAmount total = price.add(tax);

        System.out.println("║    Price: " + price.formatted());
        System.out.println("║    Tax (20%): " + tax.formatted());
        System.out.println("║    Total: " + total.formatted());

        // Records auto-generate equals — value-based comparison
        MoneyAmount a = new MoneyAmount(100, "USD");
        MoneyAmount b = new MoneyAmount(100, "USD");
        MoneyAmount c = new MoneyAmount(100, "EUR");

        System.out.println("║");
        System.out.println("║    $100 USD == $100 USD? " + a.equals(b)); // true!
        System.out.println("║    $100 USD == $100 EUR? " + a.equals(c)); // false!

        // With regular class (no equals override): a.equals(b) would be FALSE
        // because they're different objects. Records compare by VALUE.

        // Auto-generated toString
        System.out.println("║    toString: " + a);
        // Output: MoneyAmount[value=100.0, currency=USD]

        // Auto-generated hashCode — records work in HashMaps and HashSets
        java.util.Set<MoneyAmount> uniqueAmounts = java.util.Set.of(
                new MoneyAmount(100), new MoneyAmount(200), new MoneyAmount(100));
        // Wait — Set.of doesn't allow duplicates and MoneyAmount(100) appears twice
        // This would throw IllegalArgumentException because equals says they're the same!
        // That's records working correctly with collections.

        // ================================================
        // 4. Records with pattern matching (Java 21+)
        // ================================================
        System.out.println("║");
        System.out.println("║  4. Record auto-features:");

        // Destructuring in enhanced for — access components directly
        for (AccountSummary summary : summaries) {
            // Record accessors use component names (no "get" prefix):
            String name = summary.ownerName();     // not getOwnerName()
            double bal = summary.balance();         // not getBalance()
            String icon = summary.statusIcon();     // not getStatusIcon()
        }
        System.out.println("║    Record accessors: .ownerName() not .getOwnerName()");
        System.out.println("║    Auto equals: compares ALL components");
        System.out.println("║    Auto hashCode: based on ALL components");
        System.out.println("║    Auto toString: shows ALL components");
        System.out.println("║    Immutable: no setters, fields are final");

        // ================================================
        // 5. Records CANNOT do these things:
        // ================================================
        System.out.println("║");
        System.out.println("║  5. Record limitations:");
        System.out.println("║    ❌ Cannot extend a class (implicitly extends Record)");
        System.out.println("║    ❌ Cannot add mutable fields");
        System.out.println("║    ❌ Cannot be abstract");
        System.out.println("║    ✅ CAN implement interfaces");
        System.out.println("║    ✅ CAN have static fields and methods");
        System.out.println("║    ✅ CAN have custom methods");
        System.out.println("║    ✅ CAN override toString, equals, hashCode");
        System.out.println("║    ✅ CAN have compact constructors (validation)");

        System.out.println("║");
        System.out.println("╚══════════════════════════════════════════╝");
    }

    // ================================================
    // RECORDS IN METHOD SIGNATURES — type-safe returns
    // ================================================

    // Instead of returning Map<String, Object> (unsafe):
    //   return Map.of("name", "Alice", "balance", 2500.0, "status", "ACTIVE");
    //   String name = (String) result.get("name"); // cast! might fail!

    // Return a record (type-safe):
    //   return new AccountSummary("ACC-0001", "Alice", 2500.0, ACTIVE, 5, "✅");
    //   String name = result.ownerName(); // no cast, compile-time safe!

    // ================================================
    // RECORDS AS MAP KEYS — equals/hashCode for free
    // ================================================

    // Because records auto-generate equals and hashCode,
    // they work perfectly as HashMap keys:
    public static void demonstrateAsMapKey() {
        record CurrencyPair(String from, String to) {}

        Map<CurrencyPair, Double> exchangeRates = Map.of(
                new CurrencyPair("USD", "EUR"), 0.85,
                new CurrencyPair("USD", "GBP"), 0.73,
                new CurrencyPair("EUR", "GBP"), 0.86
        );

        // This works because CurrencyPair("USD", "EUR").equals(new CurrencyPair("USD", "EUR"))
        // is TRUE — records compare by value, not reference
        Double rate = exchangeRates.get(new CurrencyPair("USD", "EUR"));
        System.out.println("USD → EUR: " + rate);

        // With a regular class (no equals/hashCode override), this would return null!
    }

    // ================================================
    // LOCAL RECORDS — declared inside a method (Java 16+)
    // ================================================

    public static void demonstrateLocalRecords(List<BankAccount> accounts) {
        // Records can be declared INSIDE a method — perfect for intermediate data
        record RankEntry(int rank, String name, double balance) {
            String formatted() {
                return String.format("  #%d %-20s $%,.2f", rank, name, balance);
            }
        }

        System.out.println("--- Leaderboard (local record) ---");

        var rankings = accounts.stream()
                .sorted(Comparator.comparingDouble(BankAccount::getBalance).reversed())
                .toList();

        for (int i = 0; i < rankings.size(); i++) {
            BankAccount acc = rankings.get(i);
            RankEntry entry = new RankEntry(i + 1, acc.getOwnerName(), acc.getBalance());
            System.out.println(entry.formatted());
        }
        // RankEntry doesn't exist outside this method — perfect scope
    }
}