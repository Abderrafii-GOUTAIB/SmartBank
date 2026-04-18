package org.example.service;

import org.example.model.BankAccount;
import org.example.model.InsufficientFundsException;
import org.example.model.Transaction;
import org.example.model.TransactionType;
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
            accountManager.recordDeposit(account.getOwnerName(), amount);
            accountManager.recordTransactionCategory(TransactionType.DEPOSIT.displayName());
            Transaction lastTxn = account.getLastTransaction();
            if (lastTxn != null) {
                System.out.println(ReceiptGenerator.generateTransactionReceipt(account, lastTxn));
            }

        } catch (IllegalArgumentException e) {
            System.out.println("❌ Deposit failed: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("❌" + e.getMessage());
        }
    }

    public void processWithdrawal(BankAccount account, double amount) {
        System.out.println();
        try {
            account.withdraw(amount);
            accountManager.recordTransactionCategory("WITHDRAWAL");
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
        } catch (IllegalStateException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    public void processTransfer(String fromAccNum, String toAccNum, double amount) {
        System.out.println();
        System.out.println("Processing transfer...");

        BankAccount from = accountManager.findByAccountNumber(fromAccNum);
        BankAccount to = accountManager.findByAccountNumber(toAccNum);

        if(from == null || to == null) {
            System.out.println("❌ Transfer failed: One or both accounts not found.");
            return;
        }

        if(from.getAccountNumber().equals(to.getAccountNumber())) {
            System.out.println("❌ Cannot transfer to the same account.");
            return;
        }

        if(from.cannotTransact()) {
            System.out.println("❌ Transfer failed: Source account is " + from.getStatus().description());
            return;
        }
        if(to.cannotTransact()) {
            System.out.println("❌ Transfer failed: Destination account is " + to.getStatus().description());
            return;
        }

        try{
            from.withdraw(amount);
            accountManager.recordDeposit(from.getAccountNumber(), -amount);
            to.deposit(amount);
            accountManager.recordDeposit(to.getAccountNumber(), amount);
            System.out.println("✅ Transfer complete!");
            System.out.printf("   $%.2f: %s → %s%n", amount,
                    from.getOwnerName(), to.getOwnerName());
            System.out.printf("   %s balance: $%.2f%n",
                    from.getOwnerName(), from.getBalance());
            System.out.printf("   %s balance: $%.2f%n",
                    to.getOwnerName(), to.getBalance());
        }catch (InsufficientFundsException e) {
            System.out.println("❌ Transfer failed: " + e.getMessage());
            System.out.println("   You are short by: $"
                    + String.format("%.2f", e.getShortfall()));
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Transfer failed: " + e.getMessage());
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