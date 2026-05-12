package org.example.service;

import org.example.model.BankAccount;

import java.util.*;

public class ObjectContractDemo {

    public static void runDemo(List<BankAccount> accounts) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║       OBJECT CONTRACT DEMO               ║");
        System.out.println("╠══════════════════════════════════════════╣");

        if (accounts.isEmpty()) {
            System.out.println("║  No accounts to demo.");
            System.out.println("╚══════════════════════════════════════════╝");
            return;
        }

        BankAccount original = accounts.get(0);

        // Simulate "loading the same account from a file" — new object, same data
        BankAccount reloaded = new BankAccount(
                original.getOwnerName(),
                original.getEmail(),
                original.getBalance());

        // ═══════════════════════════════════════════
        // DEMO 1: equals()
        // ═══════════════════════════════════════════
        System.out.println("║");
        System.out.println("║  1. equals() — Identity Comparison");
        System.out.println("║");
        System.out.println("║  original: " + original);
        System.out.println("║  reloaded: " + reloaded);
        System.out.println("║");
        System.out.println("║  Same object? (==)       : " + (original == reloaded));
        System.out.println("║  Same data? (.equals())  : " + original.equals(reloaded));
        System.out.println("║");

        // Note: these will have DIFFERENT account numbers because
        // the constructor auto-generates a new one.
        // In a real scenario, we'd load from file with the SAME account number.
        // For demo purposes, let's test with the same account added twice:
        System.out.println("║  Self-comparison test:");
        System.out.println("║  original.equals(original): " + original.equals(original));

        // ═══════════════════════════════════════════
        // DEMO 2: hashCode()
        // ═══════════════════════════════════════════
        System.out.println("║");
        System.out.println("║  2. hashCode() — Bucket Assignment");
        System.out.println("║");
        System.out.println("║  original hashCode: " + original.hashCode());
        System.out.println("║  reloaded hashCode: " + reloaded.hashCode());
        System.out.println("║  Equal objects MUST have equal hashCodes.");
        System.out.println("║  Match: " + (original.equals(reloaded)
                ? "equals=true → hashCodes match ✅"
                : "equals=false → hashCodes may differ (OK)"));

        // ═══════════════════════════════════════════
        // DEMO 3: HashSet — duplicate detection
        // ═══════════════════════════════════════════
        System.out.println("║");
        System.out.println("║  3. HashSet — Duplicate Detection");
        System.out.println("║");

        Set<BankAccount> uniqueAccounts = new HashSet<>();
        for (BankAccount acc : accounts) {
            boolean added = uniqueAccounts.add(acc);
            System.out.println("║    add(" + acc.getAccountNumber()
                    + "): " + (added ? "added ✅" : "duplicate detected ❌"));
        }

        // Try adding the same account again
        boolean duplicate = uniqueAccounts.add(original);
        System.out.println("║    add(" + original.getAccountNumber()
                + ") again: " + (duplicate ? "added (BAD!)" : "duplicate detected ✅"));

        System.out.println("║    Set size: " + uniqueAccounts.size());

        // ═══════════════════════════════════════════
        // DEMO 4: HashMap — lookup by key
        // ═══════════════════════════════════════════
        System.out.println("║");
        System.out.println("║  4. HashMap — Key Lookup");
        System.out.println("║");

        Map<BankAccount, String> accountLabels = new HashMap<>();
        accountLabels.put(original, "VIP Customer");
        String label = accountLabels.get(original);
        System.out.println("║    Put original → 'VIP Customer'");
        System.out.println("║    Get original → " + label);

        // ═══════════════════════════════════════════
        // DEMO 5: toString() — meaningful display
        // ═══════════════════════════════════════════
        System.out.println("║");
        System.out.println("║  5. toString() — Human-Readable Display");
        System.out.println("║");
        System.out.println("║  Without override: BankAccount@" + Integer.toHexString(System.identityHashCode(original)));
        System.out.println("║  With override:    " + original.toString());
        System.out.println("║");
        System.out.println("║  toString() runs automatically in:");
        System.out.println("║    System.out.println(account)");
        System.out.println("║    \"Info: \" + account");
        System.out.println("║    String.format(\"%s\", account)");
        System.out.println("║    Debugger variable inspection");

        // ═══════════════════════════════════════════
        // DEMO 6: Records get this for FREE
        // ═══════════════════════════════════════════
        System.out.println("║");
        System.out.println("║  6. Records — Auto-generated Contract");
        System.out.println("║");
        System.out.println("║  Records auto-generate equals, hashCode, toString");
        System.out.println("║  based on ALL components. Zero boilerplate.");
        System.out.println("║");
        System.out.println("║  record Point(int x, int y) {}");
        System.out.println("║  new Point(3,4).equals(new Point(3,4)) → true ✅");
        System.out.println("║  new Point(3,4).hashCode() == new Point(3,4).hashCode() → true ✅");
        System.out.println("║  new Point(3,4).toString() → \"Point[x=3, y=4]\" ✅");

        // ═══════════════════════════════════════════
        // SUMMARY
        // ═══════════════════════════════════════════
        System.out.println("║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║  RULES:                                  ║");
        System.out.println("║  1. Override equals → MUST override hashCode");
        System.out.println("║  2. equals + hashCode MUST use same fields");
        System.out.println("║  3. equals: reflexive, symmetric, transitive");
        System.out.println("║  4. Records do it for free                ║");
        System.out.println("╚══════════════════════════════════════════╝");
    }
}