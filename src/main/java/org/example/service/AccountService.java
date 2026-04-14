package service;

import org.example.model.BankAccount;
import org.example.model.InsufficientFundsException;
import org.example.model.Transaction;
import org.example.service.AccountManager;
import org.example.service.ReceiptGenerator;

public class AccountService {

    // 🆕 Uses AccountManager instead of handling accounts directly
    private final AccountManager accountManager;

    public AccountService(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public void processDeposit(BankAccount account, double amount) {
        System.out.println();
        try {
            account.deposit(amount);

            // 🆕 getLastTransaction() — no more array indexing
            Transaction lastTxn = account.getLastTransaction();
            if (lastTxn != null) {
                System.out.println(ReceiptGenerator.generateTransactionReceipt(account, lastTxn));
            }

        } catch (IllegalArgumentException e) {
            System.out.println("❌ Deposit failed: " + e.getMessage());
        }
    }

    public void processWithdrawal(BankAccount account, double amount) {
        System.out.println();
        try {
            account.withdraw(amount);

            Transaction lastTxn = account.getLastTransaction();
            if (lastTxn != null) {
                System.out.println(ReceiptGenerator.generateTransactionReceipt(account, lastTxn));
            }

        } catch (InsufficientFundsException e) {
            System.out.println("❌ " + e.getMessage());
            System.out.println("   You are short by: $"
                    + String.format("%.2f", e.getShortfall()));
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Withdrawal failed: " + e.getMessage());
        }
    }

    public void printSummary() {
        System.out.println(ReceiptGenerator.generateBankSummary(
                accountManager.toArray()));
    }

    public void printAccountReport(BankAccount account) {
        System.out.println(ReceiptGenerator.generateAccountReport(account));
    }

    public void searchByName(String searchTerm) {
        // 🆕 Uses AccountManager's search — returns a List
        var results = accountManager.searchByName(searchTerm);

        System.out.println();
        System.out.println("--- Search results for: \"" + searchTerm + "\" ---");

        if (results.isEmpty()) {
            System.out.println("  No accounts found.");
            return;
        }

        for (BankAccount account : results) {
            String highlighted = ReceiptGenerator.highlightMatch(
                    account.getOwnerName(), searchTerm);
            System.out.println("  ✅ " + account.getAccountNumber()
                    + " — " + highlighted
                    + " ($" + String.format("%.2f", account.getBalance()) + ")");
        }
    }
}