package org.example;

public class BankAccount implements Displayable {

    // --- Encapsulation: private fields ---
    private String accountNumber;
    private String ownerName;
    private double balance;
    private boolean isActive;

    // --- Transaction history (arrays from Step 4) ---
    private String[] transactionHistory;
    private int transactionCount;

    // --- Constructor (basic — we'll improve this in Step 7) ---
    public BankAccount(String accountNumber, String ownerName, double initialBalance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = initialBalance;
        this.isActive = true;
        this.transactionHistory = new String[100];
        this.transactionCount = 0;
    }

    // --- Methods extracted in Step 3 ---

    public boolean deposit(double amount) {
        if (amount <= 0) {
            System.out.println("❌ Deposit amount must be positive!");
            return false;
        }
        balance += amount;
        addTransaction("DEPOSIT", amount);
        System.out.println("✅ Deposited $" + String.format("%.2f", amount));
        System.out.println("💰 New Balance: $" + String.format("%.2f", balance));
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("❌ Withdrawal amount must be positive!");
            return false;
        }
        if (amount > balance) {
            System.out.println("❌ Insufficient funds! Balance: $"
                    + String.format("%.2f", balance));
            return false;
        }
        balance -= amount;
        addTransaction("WITHDRAWAL", amount);
        System.out.println("✅ Withdrew $" + String.format("%.2f", amount));
        System.out.println("💰 New Balance: $" + String.format("%.2f", balance));
        return true;
    }

    // --- Transaction history (Step 4) ---

    private void addTransaction(String type, double amount) {
        if (transactionCount < transactionHistory.length) {
            String record = String.format("%s: $%.2f | Balance: $%.2f",
                    type, amount, balance);
            transactionHistory[transactionCount] = record;
            transactionCount++;
        }
    }

    public void printMiniStatement() {
        System.out.println("--- Mini Statement for " + ownerName + " ---");
        if (transactionCount == 0) {
            System.out.println("  No transactions yet.");
            return;
        }
        // Show last 5 transactions
        int start = Math.max(0, transactionCount - 5);
        for (int i = start; i < transactionCount; i++) {
            System.out.println("  " + (i + 1) + ". " + transactionHistory[i]);
        }
    }

    // --- Polymorphism: implementing the interface (Step 6) ---

    @Override
    public void displayInfo() {
        System.out.println("--- Account Details ---");
        System.out.println("Account #: " + accountNumber);
        System.out.println("Owner:     " + ownerName);
        System.out.println("Balance:   $" + String.format("%.2f", balance));
        System.out.println("Active:    " + (isActive ? "Yes ✅" : "No ❌"));
    }

    // --- Getters (encapsulation — controlled read access) ---

    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName()     { return ownerName; }
    public double getBalance()       { return balance; }
    public boolean isActive()        { return isActive; }

    @Override
    public String toString() {
        return String.format("Account[%s] %s — $%.2f",
                accountNumber, ownerName, balance);
    }
}