package org.example.service;

import org.example.model.BankAccount;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AccountCleaner {

    // ================================================
    // ❌ THE BUG — what NOT to do (for educational purposes)
    // ================================================

    public static void brokenCleanup(ArrayList<BankAccount> accounts) {
        System.out.println();
        System.out.println("--- Attempting BROKEN cleanup ---");
        try {
            for (BankAccount account : accounts) {
                if (account.getBalance() <= 0) {
                    accounts.remove(account); // 💥 ConcurrentModificationException!
                }
            }
        } catch (java.util.ConcurrentModificationException e) {
            System.out.println("💥 CRASH: " + e.getClass().getSimpleName());
            System.out.println("   You can't modify a list while iterating with for-each!");
        }
    }

    // ================================================
    // ✅ FIX 1: Iterator.remove() — the manual approach
    // ================================================

    public static List<BankAccount> cleanupWithIterator(List<BankAccount> accounts,
                                                        double minBalance) {
        System.out.println();
        System.out.println("--- Cleanup with Iterator.remove() ---");

        List<BankAccount> removed = new ArrayList<>();

        // Get the iterator explicitly — we need access to IT's remove method
        Iterator<BankAccount> it = accounts.iterator();

        while (it.hasNext()) {
            BankAccount account = it.next();

            if (account.getBalance() < minBalance) {
                System.out.println("  Removing: " + account);
                removed.add(account);

                // Call the ITERATOR's remove, not the list's!
                // The iterator adjusts its bookmark safely
                it.remove(); // ✅ safe — iterator knows what happened
            }
        }

        if (removed.isEmpty()) {
            System.out.println("  No accounts below $" + String.format("%.2f", minBalance));
        } else {
            System.out.println("  Removed " + removed.size() + " account(s).");
        }

        return removed;
    }

    // ================================================
    // ✅ FIX 2: removeIf() — the elegant one-liner
    // ================================================

    public static void cleanupWithRemoveIf(ArrayList<BankAccount> accounts,
                                           double minBalance) {
        System.out.println();
        System.out.println("--- Cleanup with removeIf() ---");

        int sizeBefore = accounts.size();

        // removeIf takes a Predicate (a condition that returns true/false)
        // It handles iteration + removal internally — no iterator exposed
        accounts.removeIf(account -> {
            if (account.getBalance() < minBalance) {
                System.out.println("  Removing: " + account);
                return true;  // true = remove this element
            }
            return false; // false = keep this element
        });

        int removed = sizeBefore - accounts.size();
        System.out.println("  Removed " + removed + " account(s).");
    }

    // ================================================
    // ✅ FIX 3: Collect-then-remove — the safest for complex logic
    // ================================================

    public static List<BankAccount> cleanupCollectThenRemove(List<BankAccount> accounts,
                                                             double minBalance) {
        System.out.println();
        System.out.println("--- Cleanup with collect-then-remove ---");

        // PHASE 1: Iterate and COLLECT (don't modify yet)
        List<BankAccount> toRemove = new ArrayList<>();
        for (BankAccount account : accounts) {
            if (account.getBalance() < minBalance) {
                toRemove.add(account);
            }
        }
        // Loop is done — safe to modify now

        // PHASE 2: Remove collected items AFTER the loop
        for (BankAccount account : toRemove) {
            System.out.println("  Removing: " + account);
            accounts.remove(account);
        }

        System.out.println("  Removed " + toRemove.size() + " account(s).");
        return toRemove;
    }

    // ================================================
    // ✅ PRACTICAL: Clean up flagged accounts
    // ================================================

    public static void cleanupFlagged(AccountManager manager) {
        System.out.println();
        System.out.println("--- Cleanup Flagged Accounts with Zero Balance ---");

        // Get all accounts and iterate safely
        List<BankAccount> allAccounts = new ArrayList<>(manager.getAllAccounts());
        int cleaned = 0;

        Iterator<BankAccount> it = allAccounts.iterator();
        while (it.hasNext()) {
            BankAccount account = it.next();
            String accNum = account.getAccountNumber();

            // Both conditions must be true
            if (manager.isFlagged(accNum) && account.getBalance() <= 0) {
                System.out.println("  Closing flagged empty account: " + account);
                manager.closeAccount(accNum);
                cleaned++;
            }
        }

        if (cleaned == 0) {
            System.out.println("  No flagged empty accounts to clean up.");
        } else {
            System.out.println("  Cleaned " + cleaned + " account(s).");
        }
    }
}