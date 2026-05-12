package org.example.service;

import org.example.model.BankAccount;

import java.util.*;

public class RiskService {

    private final Set<String> flaggedAccounts;
    private static final double LARGE_TRANSACTION_THRESHOLD = 10_000;

    public RiskService() {
        this.flaggedAccounts = new LinkedHashSet<>();
    }

    // --- Flagging ---

    public void flag(String accountNumber) {
        boolean added = flaggedAccounts.add(accountNumber);
        if (added) {
            System.out.println("  🚩 Account " + accountNumber + " flagged for review.");
        } else {
            System.out.println("  ℹ️  Account " + accountNumber + " was already flagged.");
        }
    }

    public void unflag(String accountNumber) {
        boolean removed = flaggedAccounts.remove(accountNumber);
        if (removed) {
            System.out.println("  ✅ Account " + accountNumber + " unflagged.");
        } else {
            System.out.println("  ℹ️  Account " + accountNumber + " was not flagged.");
        }
    }

    public boolean isFlagged(String accountNumber) {
        return flaggedAccounts.contains(accountNumber);
    }

    public Set<String> getFlaggedAccounts() {
        return Set.copyOf(flaggedAccounts);
    }

    public void loadFlags(Set<String> flags) {
        flaggedAccounts.addAll(flags);
    }

    // --- Alerts ---

    public void checkTransaction(BankAccount account, double amount, String type) {
        if (amount >= LARGE_TRANSACTION_THRESHOLD) {
            System.out.println("  🔔 ALERT: Large " + type + " of $"
                    + String.format("%,.2f", amount)
                    + " on account " + account.getAccountNumber());
            DataStore.appendLog("LARGE_TXN: " + type + " $" + amount
                    + " on " + account.getAccountNumber());
        }
    }

    // --- Risk analytics ---

    public void printFlaggedAccounts(AccountRepository repo) {
        System.out.println();
        System.out.println("--- Flagged Accounts ---");

        if (flaggedAccounts.isEmpty()) {
            System.out.println("  No flagged accounts.");
            return;
        }

        int i = 1;
        for (String accNum : flaggedAccounts) {
            repo.findByAccountNumberSafe(accNum).ifPresent(acc ->
                    System.out.println("  " + i + ". " + acc));
        }
    }
}