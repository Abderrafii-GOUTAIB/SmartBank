package org.example.service;

import org.example.model.BankAccount;

import java.util.LinkedHashSet;
import java.util.Set;

public class RiskService {

    private final Set<String> flaggedAccounts;
    private final StoragePort storage;
    private static final double LARGE_TRANSACTION_THRESHOLD = BankConfig.getInstance().getLargeTransactionThreshold();

    public RiskService(StoragePort storage) {
        this.flaggedAccounts = new LinkedHashSet<>();
        this.storage = storage;
    }

    // ── FLAGGING ──────────────────────────────────────────────────────────────

    public void flag(String accountNumber) {
        boolean added = flaggedAccounts.add(accountNumber);
        if (added) System.out.println("  🚩 Account " + accountNumber + " flagged for review.");
        else       System.out.println("  ℹ️  Account " + accountNumber + " was already flagged.");
    }

    public void unflag(String accountNumber) {
        boolean removed = flaggedAccounts.remove(accountNumber);
        if (removed) System.out.println("  ✅ Account " + accountNumber + " unflagged.");
        else         System.out.println("  ℹ️  Account " + accountNumber + " was not flagged.");
    }

    public boolean isFlagged(String accountNumber) {
        return flaggedAccounts.contains(accountNumber);
    }

    public Set<String> getFlaggedAccounts() {
        return Set.copyOf(flaggedAccounts); // defensive copy — Step 31
    }

    public void loadFlags(Set<String> flags) {
        flaggedAccounts.addAll(flags);
    }

    // ── ALERTS ────────────────────────────────────────────────────────────────

    public void checkTransaction(BankAccount account, double amount, String type) {
        if (amount >= LARGE_TRANSACTION_THRESHOLD) {
            System.out.println("  🔔 ALERT: Large " + type + " of $"
                    + String.format("%,.2f", amount)
                    + " on account " + account.getAccountNumber());
            storage.logAudit("LARGE_TXN: " + type + " $" + amount
                    + " on " + account.getAccountNumber());
        }
    }

    // ── REPORTS ───────────────────────────────────────────────────────────────

    public void printFlaggedAccounts(AccountRepository repo) {
        System.out.println();
        System.out.println("--- Flagged Accounts ---");

        if (flaggedAccounts.isEmpty()) {
            System.out.println("  No flagged accounts.");
            return;
        }

        // int[] trick — lambda needs effectively final variable (Phase 3)
        int[] counter = {1};
        flaggedAccounts.forEach(accNum ->
                repo.findByAccountNumberSafe(accNum).ifPresent(acc ->
                        System.out.println("  " + counter[0]++ + ". " + acc)));
    }
}