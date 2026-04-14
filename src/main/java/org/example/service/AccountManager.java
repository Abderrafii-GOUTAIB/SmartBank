package org.example.service;

import org.example.model.BankAccount;

import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private final List<BankAccount> accounts;

    public AccountManager() {
        this.accounts = new ArrayList<>();
    }

    public BankAccount createAccount(String ownerName, double initialBalance) {
        try {
            BankAccount account = new BankAccount(ownerName, initialBalance);
            accounts.add(account); // 🆕 add to dynamic list
            System.out.println("📋 Created: " + account);
            return account;
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Cannot create account: " + e.getMessage());
            return null;
        }
    }
    // Linear search through the list — we'll replace this with HashMap in Step 13
    public BankAccount findByAccountNumber(String accountNumber) {
        // Enhanced for-each — works the same as with arrays
        for (BankAccount account : accounts) {
            // .equals() for String comparison — never ==
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }
        return null; // not found
    }

    public BankAccount createAccount(String ownerName) {
        return createAccount(ownerName, 0.0);
    }

    public BankAccount getAccountByIndex(int index) {
        if (index < 0 || index >= accounts.size()) {
            System.out.println("❌ Invalid index: " + (index + 1));
            return null;
        }
        return accounts.get(index);
    }

    public BankAccount findAccountNumber(String number) {
        for (BankAccount account : accounts) {
            if (account.getOwnerName().equals(number)) {
                return account;
            }
        }
        return null;
    }

    // .contains() relies on .equals() — we haven't overridden it yet
    // so for now we search manually
    public List<BankAccount> searchByName(String searchTerm) {
        // 🆕 Build a RESULT list — dynamic, add matches as we find them
        List<BankAccount> results = new ArrayList<>();

        for (BankAccount account : accounts) {
            if (account.getOwnerName().toLowerCase()
                    .contains(searchTerm.toLowerCase())) {
                results.add(account);
            }
        }

        return results; // could be empty, that's fine
    }
    // .remove(object) — remove by reference
    // .remove(index) — remove by position
    public boolean closeAccount(String accountNumber) {
        BankAccount account = findByAccountNumber(accountNumber);
        if (account == null) {
            System.out.println("❌ Account not found: " + accountNumber);
            return false;
        }

        if (account.getBalance() > 0) {
            System.out.println("⚠️  Account still has $"
                    + String.format("%.2f", account.getBalance())
                    + " — withdraw funds first.");
            return false;
        }

        // .remove(object) — removes the first occurrence
        // Returns true if the element was found and removed
        boolean removed = accounts.remove(account);
        if (removed) {
            System.out.println("🗑️  Account " + accountNumber + " closed.");
        }
        return removed;
    }

    // .size() — number of elements
    public int getAccountCount() {
        return accounts.size();
    }

    // .isEmpty() — is the list empty?
    public boolean hasAccounts() {
        return !accounts.isEmpty();
    }

    // Return unmodifiable view — outsiders can read but not modify our list
    public List<BankAccount> getAllAccounts() {
        // List.copyOf() — immutable copy (safe to share)
        return List.copyOf(accounts);
    }

    // Print numbered list for menu selection
    public void printAccountList() {
        if (accounts.isEmpty()) {
            System.out.println("  No accounts yet.");
            return;
        }

        // Traditional for loop — we need the index for numbering
        for (int i = 0; i < accounts.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + accounts.get(i));
        }
    }

    // Convert to array for methods that need BankAccount[]
    // (like ReceiptGenerator.generateBankSummary)
    public BankAccount[] toArray() {
        // .toArray(new T[0]) — convert List to array
        return accounts.toArray(new BankAccount[0]);
    }
}


