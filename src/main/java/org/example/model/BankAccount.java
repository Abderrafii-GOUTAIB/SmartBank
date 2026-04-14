package org.example.model;

import org.example.model.Displayable;

public class BankAccount implements Displayable {

    // ============================================================
    // ACCESS MODIFIERS IN ACTION — watch the visibility choices
    // ============================================================

    // PRIVATE STATIC — only this class manages the counter
    // No one outside should touch the ID generator
    private static int nextId = 1;

    // PUBLIC STATIC FINAL — everyone can READ the bank name, no one can change it
    public static final String BANK_NAME = "SmartBank";

    // PRIVATE — only this class touches these directly
    // Outside code uses getters (controlled access)
    private final String accountNumber;
    private final String ownerName;
    private double balance;
    private boolean isActive;
    private Transaction[] transactions;
    private int transactionCount;

    // --- Constructors (PUBLIC — other packages need to create accounts) ---

    public BankAccount(String ownerName, double initialBalance) {
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("Owner name cannot be empty.");
        }
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative.");
        }
        this.accountNumber = generateAccountNumber();
        this.ownerName = ownerName;
        this.balance = initialBalance;
        this.isActive = true;
        this.transactions = new Transaction[100];
        this.transactionCount = 0;
    }

    public BankAccount(String ownerName) {
        this(ownerName, 0.0);
    }

    public BankAccount(BankAccount other) {
        this(other.ownerName, 0.0);
    }

    // PRIVATE STATIC — internal utility, no one outside needs this
    private static String generateAccountNumber() {
        String accNum = String.format("ACC-%04d", nextId);
        nextId++;
        return accNum;
    }

    // PUBLIC STATIC — useful query, safe to expose
    public static int getTotalAccounts() {
        return nextId - 1;
    }

    // --- PUBLIC methods — the API we expose ---

    public boolean deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        balance += amount;
        addTransaction("DEPOSIT", amount);
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        if (amount > balance) {
            throw new InsufficientFundsException(amount, balance);

        }
        balance -= amount;
        addTransaction("WITHDRAWAL", amount);
        return true;
    }

    // PRIVATE — internal bookkeeping, no one outside needs to call this
    private void addTransaction(String type, double amount) {
        if (transactionCount < transactions.length) {
            transactions[transactionCount] = new Transaction(accountNumber, type, amount, balance);
            transactionCount++;
        }
    }

    public void printMiniStatement() {
        System.out.println("--- Mini Statement for " + ownerName + " ---");
        if (transactionCount == 0) {
            System.out.println("  No transactions yet.");
            return;
        }
        int start = Math.max(0, transactionCount - 5);
        for (int i = start; i < transactionCount; i++) {
            System.out.println("  " + (i + 1) + ". " + transactions[i]);
        }
    }
    // 🆕 Get a copy of transactions (we'll improve this with Collections next)
    public Transaction[] getTransactions() {
        Transaction[] copy = new Transaction[transactionCount];
        for (int i = 0; i < transactionCount; i++) {
            copy[i] = transactions[i];
        }
        return copy;
    }


    // --- Displayable interface implementation ---

    @Override
    public void displayInfo() {
        System.out.println("--- Account Details ---");
        System.out.println("Bank:      " + BANK_NAME);
        System.out.println("Account #: " + accountNumber);
        System.out.println("Owner:     " + ownerName);
        System.out.println("Balance:   $" + String.format("%.2f", balance));
        System.out.println("Transactions:" +transactionCount);
        System.out.println("Active:    " + (isActive ? "Yes ✅" : "No ❌"));
    }

    // --- PUBLIC getters — controlled read-only access ---

    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName()     { return ownerName; }
    public double getBalance()       { return balance; }
    public boolean isActive()        { return isActive; }
    public int getTransactionCount() { return transactionCount; }

    @Override
    public String toString() {
        return String.format("[%s] %s — $%.2f", accountNumber, ownerName, balance);
    }
}