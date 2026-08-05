package org.example.service;

import org.example.model.BankAccount;
import org.example.model.Transaction;

import java.util.List;
import java.util.Set;

/**
 * DIP: High-level services depend on THIS, not on DataStore directly.
 * DataStore implements this. Tomorrow, DatabaseStorage will too.
 */
public interface StoragePort {

    // ── SAVE ──────────────────────────────────────────
    void saveState(List<BankAccount> accounts);
    void saveFlaggedAccounts(Set<String> flagged);
    void logAudit(String entry);
    String exportTransactionsCSV(List<Transaction> transactions, String accountNumber);

    // ── LOAD ──────────────────────────────────────────
    List<BankAccount> loadAccounts();
    Set<String> loadFlaggedAccounts();
}