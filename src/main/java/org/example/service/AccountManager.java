package org.example.service;

import org.example.model.BankAccount;

import java.util.*;

public class AccountManager {
    private final List<BankAccount> accountList;
    private final Map<String, BankAccount> accountMap; // 🆕 for fast lookup by account number
    private final Map<String, Double> dailyDeposits;

    private final Set<String>  registredEmails; // 🆕 track registered emails for uniqueness
    private final Set<String> transactionCategories;
    private final Set<String> flaggedAccounts;



    public AccountManager() {
        this.accountList = new ArrayList<>();
        this.dailyDeposits = new HashMap<>();
        this.accountMap = new HashMap<>();

        this.registredEmails = new HashSet<>();
        this.transactionCategories = new TreeSet<>();
        this.flaggedAccounts = new LinkedHashSet<>();
    }

    public BankAccount createAccount(String ownerName,String email, double initialBalance) {
        try {
            String normalizedEmail = email == null ? "" : email.trim().toLowerCase();

            if(registredEmails.contains(normalizedEmail)){
                throw new IllegalArgumentException("Email already registered");
            }

            BankAccount account = new BankAccount(ownerName,normalizedEmail ,initialBalance);
            accountList.add(account); // 🆕 add to dynamic list
            accountMap.put(account.getAccountNumber(), account);
            dailyDeposits.put(account.getOwnerName(), initialBalance);

            registredEmails.add(account.getEmail());
            System.out.println("📋 Created: " + account);
            return account;
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Cannot create account: " +  email + " :" + e.getMessage());
            return null;
        }
    }

    public BankAccount createAccount(String ownerName, String email) {
        return createAccount(ownerName, email,0.0);
    }
    public void recordTransactionCategory( String category) {
            transactionCategories.add(category);
    }

    public Set<String> getUniqueCategories() {
        return Set.copyOf(transactionCategories);
    }

    public void printCategories() {
        System.out.println();
        System.out.println("--- Unique Transaction Categories ---");

        if (transactionCategories.isEmpty()) {
            System.out.println("  No transactions recorded yet.");
            return;
        }

        // TreeSet iteration is ALWAYS in sorted order
        int i = 1;
        for (String category : transactionCategories) {
            System.out.println("  " + i + ". " + category);
            i++;
        }
    }

    public void flagAccount(String accountNumber) {
        if (!accountMap.containsKey(accountNumber)) {
            System.out.println("❌ Account not found: " + accountNumber);
            return;
        }

        boolean flagged = flaggedAccounts.contains(accountNumber);
        if (flagged) {
            System.out.println("ℹ️  " + accountNumber + " was already flagged.");

        } else {
            flaggedAccounts.add(accountNumber);
            System.out.println("🚩 Flagged: " + accountNumber);
        }

    }

    public void unflagAccount(String accountNumber) {
        boolean removed = flaggedAccounts.remove(accountNumber);
        if (removed) {
            System.out.print("✅ Unflagged: " + accountNumber);
        }else {
            System.out.println("ℹ️  " + accountNumber + " was not flagged.");
        }

    }

    public boolean isFlagged(String accountNumber) {
        return flaggedAccounts.contains(accountNumber);
    }

    public void printFlaggedAccounts() {
        System.out.println();
        System.out.println("--- Flagged Accounts (in order flagged) ---");
        if (flaggedAccounts.isEmpty()) {
            System.out.println("  No flagged accounts.");
            return;
        }
        int index = 1;
        for (String accNum : flaggedAccounts) {
            BankAccount account = accountMap.get(accNum);  // ✅ Should not be null if keys match
            if (account != null) {
                System.out.println("  " + index + ". " + accNum + " (" + account.getOwnerName() + ")");
                index++;
            }
        }
    }

    // 🆕 Helper method - find all rich accounts
    private Set<String> getRichAccounts(double minBalance) {
        Set<String> richAccounts = new HashSet<>();
        for (BankAccount account : accountList) {
            if(account.getBalance() >= minBalance) {
                richAccounts.add(account.getOwnerName());
            }
        }
        return richAccounts;
    }


    public Set<String> getRichFlaggedAccounts(double minBalance) {
        Set<String> results = new HashSet<>(getRichAccounts(minBalance));
        results.retainAll(flaggedAccounts);  // Keep only flagged
        return results;
    }

    // ✨ Refactored - uses helper method
    public Set<String> getUnflaggedRichAccounts(double minBalance) {
        Set<String> results = new HashSet<>(getRichAccounts(minBalance));
        results.removeAll(flaggedAccounts);  // Remove flagged ones
        return results;
    }

    // Linear search through the list — we'll replace this with HashMap in Step 13
    public BankAccount findByAccountNumber(String accountNumber) {


        BankAccount account = accountMap.get(accountNumber);
        if(account == null) {
            System.out.println("❌ Account not found: " + accountNumber);
        }
        return account;
    }



    public Boolean accountExists(String accountNumber) {
        return accountMap.containsKey(accountNumber);
    }

    public BankAccount getAccountByIndex(int index) {
        if (index < 0 || index >= accountList.size()) {
            System.out.println("❌ Invalid index: " + (index + 1));
            return null;
        }
        return accountList.get(index);
    }

    // .contains() relies on .equals() — we haven't overridden it yet
    // so for now we search manually
    public List<BankAccount> searchByName(String searchTerm) {
        // 🆕 Build a RESULT list — dynamic, add matches as we find them
        List<BankAccount> results = new ArrayList<>();

        for (BankAccount account : accountList) {
            if (account.getOwnerName().toLowerCase()
                    .contains(searchTerm.toLowerCase())) {
                results.add(account);
            }
        }

        return results; // could be empty, that's fine
    }

    public void recordDeposit(String accountNumber, double amount) {
        double currentTotal = dailyDeposits.getOrDefault(accountNumber, 0.0);
        dailyDeposits.put(accountNumber, currentTotal + amount);
    }

    public double getDailyDepositTotal(String accountNumber) {
        return dailyDeposits.getOrDefault(accountNumber, 0.0);
    }

    public double getTotalDailyDeposits() {
        double total = 0.0;
        for (double amount : dailyDeposits.values()) {
            total += amount;
        }
        return total;
    }

    // 🆕 Print daily deposit report
    public void printDailyDepositReport() {
        System.out.println();
        System.out.println("--- Daily Deposit Report ---");

        if (dailyDeposits.isEmpty()) {
            System.out.println("  No deposits today.");
            return;
        }

        // .entrySet() — returns all key-value pairs as Set<Map.Entry>
        // Each Entry has .getKey() and .getValue()
        for (Map.Entry<String, Double> entry : dailyDeposits.entrySet()) {
            String accNum = entry.getKey();
            double total = entry.getValue();

            if (total > 0) {
                // 🆕 Use the accountMap to look up the name instantly
                BankAccount account = accountMap.get(accNum);
                String name = (account != null) ? account.getOwnerName() : "Unknown";

                System.out.printf("  %s (%s): $%.2f%n", accNum, name, total);
            }
        }

        System.out.printf("  TOTAL: $%.2f%n", getTotalDailyDeposits());
    }

    // .remove(object) — remove by reference
    // .remove(index) — remove by position
    public boolean closeAccount(String accountNumber) {
        BankAccount account = accountMap.get(accountNumber);
        if (account == null) {
            System.out.println("❌ Account not found: " + accountNumber);
            return false;
        }
        if(account.getBalance() <= 0) {
            System.out.println("❌ Cannot close account with non-positive balance: $" + String.format("%.2f", account.getBalance()));
            return false;
        }
        accountList.remove(account);
        accountMap.remove(accountNumber);
        dailyDeposits.remove(account.getOwnerName());
        registredEmails.remove(account.getEmail());
        flaggedAccounts.remove(accountNumber);

        System.out.println("🗑️  Account " + accountNumber + " closed.");
        return true;
    }

    // .size() — number of elements
    public int getAccountCount() {
        return accountList.size();
    }

    // .isEmpty() — is the list empty?
    public boolean hasAccounts() {
        return !accountList.isEmpty();
    }

    // Return unmodifiable view — outsiders can read but not modify our list
    public List<BankAccount> getAllAccounts() {
        // List.copyOf() — immutable copy (safe to share)
        return List.copyOf(accountList);
    }

    // ✅ No param - displays ALL accounts
    public void printAccountList() {
        printAccountList(null);  // Delegate to overloaded version
    }

    // ✅ With param - hides the current account
    public void printAccountList(BankAccount currentAccount) {
        if (accountList.isEmpty()) {
            System.out.println("  No accounts yet.");
            return;
        }

        int displayIndex = 1;
        for (BankAccount account : accountList) {
            // Skip current account if provided
            if (account.equals(currentAccount)) {
                continue;
            }

            System.out.println("  " + displayIndex + ". " + account);
            displayIndex++;
        }
    }


    // Convert to array for methods that need BankAccount[]
    // (like ReceiptGenerator.generateBankSummary)
    public BankAccount[] toArray() {
        // .toArray(new T[0]) — convert List to array
        return accountList.toArray(new BankAccount[0]);
    }

    public void printMapDebug() {
        System.out.println();
        System.out.println("--- HashMap Debug View ---");
        System.out.println("Map size: " + accountMap.size());

        // .keySet() — returns all keys as a Set
        System.out.println("Keys: " + accountMap.keySet());

        // .values() — returns all values as a Collection
        System.out.println("Values: " + accountMap.values());

        // .entrySet() — returns all entries
        for (Map.Entry<String, BankAccount> entry : accountMap.entrySet()) {
            System.out.println("  " + entry.getKey() + " → " + entry.getValue());
        }
    }
}


