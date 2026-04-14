package org.example.service;


import org.example.model.BankAccount;
import org.example.model.InsufficientFundsException;  // ⬅️ ADD THIS
import org.example.service.ReceiptGenerator;

public class AccountService {

    public BankAccount createAccount(String ownerName, double initialBalance) {
        try {
            BankAccount account = new BankAccount(ownerName, initialBalance);
            System.out.println("📋 Created: " + account);
            return account;
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Cannot create account: " + e.getMessage());
            return null;
        }
    }

    public BankAccount createAccount(String ownerName) {
        return createAccount(ownerName, 0.0);
    }

    public void processDeposit(BankAccount account, double amount) {
        System.out.println();
        try {
            account.deposit(amount);

            // 🆕 Generate a receipt for the last transaction
            var transactions = account.getTransactions();
            var lastTxn = transactions[transactions.length - 1];
            System.out.println(ReceiptGenerator.generateTransactionReceipt(account, lastTxn));

        } catch (IllegalArgumentException e) {
            System.out.println("❌ Deposit failed: " + e.getMessage());
        }
    }

    public void processWithdrawal(BankAccount account, double amount) {
        System.out.println();
        try {
            account.withdraw(amount);

            // 🆕 Generate a receipt
            var transactions = account.getTransactions();
            var lastTxn = transactions[transactions.length - 1];
            System.out.println(ReceiptGenerator.generateTransactionReceipt(account, lastTxn));

        } catch (InsufficientFundsException e) {
            System.out.println("❌ " + e.getMessage());
            System.out.println("   You are short by: $"
                    + String.format("%.2f", e.getShortfall()));
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Withdrawal failed: " + e.getMessage());
        }
    }

    // 🆕 Now returns a String instead of printing directly
    // This means we can print it, save it to file, send it — anything
    public void printSummary(BankAccount... accounts) {
        System.out.println(ReceiptGenerator.generateBankSummary(accounts));
    }

    // 🆕 Print full account report with transaction history
    public void printAccountReport(BankAccount account) {
        System.out.println(ReceiptGenerator.generateAccountReport(account));
    }

    // 🆕 Search accounts by name
    public void searchByName(String searchTerm, BankAccount... accounts) {
        System.out.println();
        System.out.println("--- Search results for: \"" + searchTerm + "\" ---");

        boolean found = false;
        for (BankAccount account : accounts) {
            if (account == null) continue;

            // .toLowerCase() for case-insensitive comparison
            // .contains() to check if searchTerm exists in the name
            if (account.getOwnerName().toLowerCase().contains(searchTerm.toLowerCase())) {
                // 🆕 Highlight the matching part
                String highlighted = ReceiptGenerator.highlightMatch(
                        account.getOwnerName(), searchTerm);
                System.out.println("  ✅ " + account.getAccountNumber()
                        + " — " + highlighted
                        + " ($" + String.format("%.2f", account.getBalance()) + ")");
                found = true;
            }
        }

        if (!found) {
            System.out.println("  No accounts found matching \"" + searchTerm + "\"");
        }
    }
}