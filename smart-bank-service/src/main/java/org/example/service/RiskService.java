package org.example.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class RiskService {

    private final Set<String> flaggedAccounts;

    public RiskService() {
        this.flaggedAccounts = new LinkedHashSet<>();
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
                repo.findByAccountNumber(accNum).ifPresent(acc ->
                        System.out.println("  " + counter[0]++ + ". " + acc)));
    }
}