package org.example.service;

import org.example.model.BankAccount;
import org.example.model.Transaction;
import org.example.model.TransactionType;
import org.example.model.exception.*;

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

        } catch (InvalidAmountException e) {
            System.out.println("❌ " + e.toDisplayString());
        } catch (AccountFrozenException e) {
            System.out.println("❌ " + e.toDisplayString());
            System.out.println("   Current status: " + e.getStatus().icon()
                    + " " + e.getStatus().description());
        } catch (BankException e) {
            // Catch-all for any bank exception we didn't handle specifically
            System.out.println("❌ " + e.toDisplayString());
        }
    }

    public void processWithdrawal(BankAccount account, double amount) {
        System.out.println();
        try {
            account.withdraw(amount);
            accountManager.recordTransactionCategory(TransactionType.WITHDRAWAL.displayName());

            Transaction lastTxn = account.getLastTransaction();
            if (lastTxn != null) {
                System.out.println(ReceiptGenerator.generateTransactionReceipt(account, lastTxn));
            }

        } catch (InsufficientFundsException e) {
            System.out.println("❌ " + e.toDisplayString());
            System.out.printf("   Requested:  $%.2f%n", e.getRequested());
            System.out.printf("   Available:  $%.2f%n", e.getAvailable());
            System.out.printf("   Short by:   $%.2f%n", e.getShortfall());
        } catch (AccountFrozenException e) {
            System.out.println("❌ " + e.toDisplayString());
        } catch (InvalidAmountException e) {
            System.out.println("❌ " + e.toDisplayString());
        } catch (BankException e) {
            System.out.println("❌ " + e.toDisplayString());
        }
    }

    public void processTransfer(String fromAccNum, String toAccNum, double amount) {
        System.out.println();
        System.out.println("Processing transfer...");

        try {
            BankAccount from = accountManager.findByAccountNumber(fromAccNum);
            BankAccount to = accountManager.findByAccountNumber(toAccNum);

            if (from.getAccountNumber().equals(to.getAccountNumber())) {
                System.out.println("❌ Cannot transfer to the same account.");
                return;
            }

            from.withdraw(amount);
            to.deposit(amount);
            accountManager.recordDeposit(to.getAccountNumber(), amount);
            accountManager.recordTransactionCategory(TransactionType.TRANSFER_IN.displayName());
            accountManager.recordTransactionCategory(TransactionType.TRANSFER_OUT.displayName());

            System.out.println("✅ Transfer complete!");
            System.out.printf("   $%.2f: %s → %s%n", amount,
                    from.getOwnerName(), to.getOwnerName());
            System.out.printf("   %s balance: $%.2f%n",
                    from.getOwnerName(), from.getBalance());
            System.out.printf("   %s balance: $%.2f%n",
                    to.getOwnerName(), to.getBalance());

            // 🆕 Each exception type gets specific handling
        } catch (AccountNotFoundException e) {
            System.out.println("❌ " + e.toDisplayString());
        } catch (InsufficientFundsException e) {
            System.out.println("❌ Transfer failed — " + e.getMessage());
            System.out.printf("   Short by: $%.2f%n", e.getShortfall());
        } catch (AccountFrozenException e) {
            System.out.println("❌ " + e.toDisplayString());
        } catch (BankException e) {
            System.out.println("❌ " + e.toDisplayString());
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