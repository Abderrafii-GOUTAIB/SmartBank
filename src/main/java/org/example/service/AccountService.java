package org.example.service;

import org.example.model.*;

// We must IMPORT classes from other packages
// This is like saying "I need the BankAccount blueprint from the model folder"
public class AccountService {
    // This service handles all account operations
    // It talks to BankAccount through PUBLIC methods only
    // It CANNOT access private fields like 'balance' directly

    public BankAccount createAccount(String ownerName , double balance) {
        try {
            BankAccount account = new BankAccount(ownerName, balance);
            System.out.println("✅ Account created for " + ownerName);
            return account;
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Failed to create account: " + e.getMessage());
            return null;
        }
    }

    public BankAccount createAccount( String ownerName){
        return createAccount(ownerName, 0.0);
    }

    public void processDeposit(BankAccount account, double amount) {
        System.out.println();
        System.out.println("Processing deposit for " + account.getOwnerName() + "...");
        try {
            account.deposit(amount);
            System.out.println("✅ Success! New balance: $"
                    + String.format("%.2f", account.getBalance()));
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Deposit failed: " + e.getMessage());
        }
    }

    public void processWithdrawal(BankAccount account, double amount) {
        System.out.println();
        System.out.println("Processing withdrawal for " + account.getOwnerName() + "...");
        try {
            account.withdraw(amount);
            System.out.println("✅ Success! New balance: $"
                    + String.format("%.2f", account.getBalance()));
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Withdrawal failed: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.println("❌ Withdrawal failed: " + e.getMessage());
        }
    }

    public void printSummary(BankAccount... account) {
        // BankAccount... is varargs — accepts any number of accounts
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║         " + BankAccount.BANK_NAME + " — Account Summary          ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");

        double totalBalance = 0;
        System.out.println("║  Account Number  │ Owner Name       │ Balance        │ Status    ║");
        for (BankAccount bankAccount : account) {
            totalBalance += bankAccount.getBalance();
        }
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf(" ║  Total (%d accounts)              $%10.2f                        ║%n",
                BankAccount.getTotalAccounts(), totalBalance);
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");

    }

}
