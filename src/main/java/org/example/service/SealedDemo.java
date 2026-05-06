package org.example.service;

import org.example.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SealedDemo {

    // ================================================
    // PATTERN MATCHING WITH SEALED TYPES (Java 21+)
    // ================================================

    // Because AccountType is sealed, the compiler KNOWS all possible types.
    // No 'default' case needed — the switch is exhaustive!

    public static double calculateInterest(AccountType type, double balance) {
        // The compiler verifies ALL permitted types are handled:
        return switch (type) {
            case AccountType.Savings s   -> balance * s.interestRate();
            case AccountType.Checking c  -> balance * c.interestRate();
            case AccountType.Business b  -> balance * b.interestRate();
            // No default! Compiler knows these are the ONLY three options.
            // If someone adds a new permitted type, this code WON'T COMPILE
            // until they add a case for it — no silent bugs!
        };
    }

    // Process a transaction result — sealed ensures exhaustive handling
    public static String describeResult(TransactionResult result) {
        return switch (result) {
            case TransactionResult.Success s ->
                    "Processed: " + s.type().displayName()
                            + " of " + s.type().formatAmount(s.amount())
                            + " → Balance: $" + String.format("%,.2f", s.balanceAfter());

            case TransactionResult.Failure f ->
                    "Failed [" + f.errorCode() + "]: " + f.reason();
            // Again — no default needed. Success or Failure — that's it.
        };
    }

    // Process bank events — multi-level sealed hierarchy
    public static String describeEvent(BankEvent event) {
        return switch (event) {
            case BankEvent.AccountEvent.AccountCreated e ->
                    "📋 New account: " + e.getOwnerName() + " (" + e.getAccountNumber() + ")";

            case BankEvent.AccountEvent.AccountClosed e ->
                    "🗑️ Closed: " + e.getAccountNumber();

            case BankEvent.AccountEvent.StatusChanged e ->
                    "🔄 " + e.getAccountNumber() + ": " + e.getFrom() + " → " + e.getTo();

            case BankEvent.TransactionEvent e ->
                    "💰 " + e.getType().displayName() + " "
                            + e.getType().formatAmount(e.getAmount())
                            + " on " + e.getAccountNumber();

            // SystemEvent is non-sealed — could have unknown subtypes
            // So we need a broader match here
            case BankEvent.SystemEvent e ->
                    "⚙️ " + e.getDescription();
        };
    }

    // ================================================
    // PRACTICAL DEMO
    // ================================================

    public static void runDemo(List<BankAccount> accounts) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║          SEALED CLASSES DEMO             ║");
        System.out.println("╠══════════════════════════════════════════╣");

        // --- 1. AccountType — sealed interface with records ---
        System.out.println("║");
        System.out.println("║  1. AccountType (sealed interface):");
        System.out.println("║");

        AccountType savings = new AccountType.Savings();
        AccountType checking = new AccountType.Checking();
        AccountType business = new AccountType.Business();

        List<AccountType> types = List.of(savings, checking, business);

        for (AccountType type : types) {
            System.out.println("║    " + type.summary());
        }

        // Calculate interest for a sample balance
        double balance = 10_000;
        System.out.println("║");
        System.out.printf("║  Interest on $%,.2f:%n", balance);

        for (AccountType type : types) {
            double interest = calculateInterest(type, balance);
            System.out.printf("║    %-10s → $%,.2f/year%n",
                    type.displayName(), interest);
        }

        // --- 2. TransactionResult — sealed success/failure ---
        System.out.println("║");
        System.out.println("║  2. TransactionResult (sealed Success | Failure):");
        System.out.println("║");

        List<TransactionResult> results = List.of(
                new TransactionResult.Success("ACC-0001", 500, 3000,
                        TransactionType.DEPOSIT),
                new TransactionResult.Success("ACC-0001", 200, 2800,
                        TransactionType.WITHDRAWAL),
                new TransactionResult.Failure("ACC-0002", 99999,
                        "TXN_INSUFFICIENT_FUNDS", "Balance too low"),
                new TransactionResult.Failure("ACC-0003", 500,
                        "ACC_FROZEN", "Account is frozen")
        );

        for (TransactionResult result : results) {
            System.out.println("║    " + describeResult(result));
        }

        // Use streams with sealed types
        long successes = results.stream()
                .filter(TransactionResult::isSuccess)
                .count();
        long failures = results.stream()
                .filter(r -> !r.isSuccess())
                .count();

        System.out.println("║");
        System.out.printf("║    Results: %d success, %d failure%n", successes, failures);

        // Sum only successful amounts
        double totalSuccess = results.stream()
                .filter(r -> r instanceof TransactionResult.Success)
                .mapToDouble(TransactionResult::amount)
                .sum();
        System.out.printf("║    Total successful: $%,.2f%n", totalSuccess);

        // --- 3. BankEvent — multi-level sealed hierarchy ---
        System.out.println("║");
        System.out.println("║  3. BankEvent (multi-level sealed hierarchy):");
        System.out.println("║");

        List<BankEvent> eventLog = new ArrayList<>();

        // Simulate events
        eventLog.add(new BankEvent.AccountEvent.AccountCreated("ACC-0001", "Alice"));
        eventLog.add(new BankEvent.TransactionEvent("ACC-0001",
                TransactionType.DEPOSIT, 5000));
        eventLog.add(new BankEvent.AccountEvent.AccountCreated("ACC-0002", "Bob"));
        eventLog.add(new BankEvent.TransactionEvent("ACC-0002",
                TransactionType.DEPOSIT, 1000));
        eventLog.add(new BankEvent.AccountEvent.StatusChanged("ACC-0002",
                AccountStatus.ACTIVE, AccountStatus.FROZEN));
        eventLog.add(new BankEvent.TransactionEvent("ACC-0001",
                TransactionType.WITHDRAWAL, 500));
        eventLog.add(new BankEvent.SystemEvent("Daily backup completed"));
        eventLog.add(new BankEvent.AccountEvent.AccountClosed("ACC-0003"));

        // Print all events using pattern matching
        System.out.println("║    Event log:");
        for (BankEvent event : eventLog) {
            System.out.println("║      " + describeEvent(event));
        }

        // Group events by type
        Map<String, Long> eventCounts = eventLog.stream()
                .collect(Collectors.groupingBy(
                        BankEvent::getEventType,
                        Collectors.counting()));

        System.out.println("║");
        System.out.println("║    Event counts:");
        eventCounts.forEach((type, count) ->
                System.out.println("║      " + type + ": " + count));

        // --- 4. Hierarchy visualization ---
        System.out.println("║");
        System.out.println("║  4. Sealed hierarchy structure:");
        System.out.println("║");
        System.out.println("║    sealed AccountType");
        System.out.println("║    ├── record Savings   (final — record is always final)");
        System.out.println("║    ├── record Checking  (final)");
        System.out.println("║    └── record Business  (final)");
        System.out.println("║");
        System.out.println("║    sealed TransactionResult");
        System.out.println("║    ├── record Success   (final)");
        System.out.println("║    └── record Failure   (final)");
        System.out.println("║");
        System.out.println("║    sealed BankEvent");
        System.out.println("║    ├── sealed AccountEvent");
        System.out.println("║    │   ├── final AccountCreated");
        System.out.println("║    │   ├── final AccountClosed");
        System.out.println("║    │   └── final StatusChanged");
        System.out.println("║    ├── final TransactionEvent");
        System.out.println("║    └── non-sealed SystemEvent    ← open for extension");

        // --- 5. Why sealed matters ---
        System.out.println("║");
        System.out.println("║  5. Why sealed matters:");
        System.out.println("║");
        System.out.println("║    ✅ Compiler checks ALL cases in switch — no missing branches");
        System.out.println("║    ✅ No 'default' needed — exhaustive by design");
        System.out.println("║    ✅ Adding a new type FORCES you to update all switches");
        System.out.println("║    ✅ Nobody can create unauthorized subtypes");
        System.out.println("║    ✅ Combines perfectly with records (sealed + record = power)");
        System.out.println("║    ✅ Pattern matching extracts data + checks type in one step");

        System.out.println("║");
        System.out.println("╚══════════════════════════════════════════╝");
    }

    // ================================================
    // EXTENDING NON-SEALED — allowed because SystemEvent is non-sealed
    // ================================================

    // This compiles because SystemEvent is non-sealed:
    static class AuditEvent extends BankEvent.SystemEvent {
        private final String auditor;

        AuditEvent(String description, String auditor) {
            super(description);
            this.auditor = auditor;
        }

        @Override
        public String getEventType() { return "AUDIT"; }
        public String getAuditor() { return auditor; }
    }

    // This would NOT compile:
    // static class HackerAccount extends AccountType.Savings { }
    // ❌ Error: "cannot inherit from final class Savings"

    // This would NOT compile either:
    // record FakeType() implements AccountType { }
    // ❌ Error: "FakeType is not allowed in the sealed hierarchy of AccountType"
}