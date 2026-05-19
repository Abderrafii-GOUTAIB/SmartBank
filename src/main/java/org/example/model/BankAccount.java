package org.example.model;

import org.example.model.exception.AccountFrozenException;
import org.example.model.exception.InsufficientFundsException;
import org.example.model.exception.InvalidAmountException;
import org.example.model.exception.InvalidStatusTransitionException;
import org.example.service.BankConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BankAccount implements Displayable {

    // ============================================================
    // ACCESS MODIFIERS IN ACTION — watch the visibility choices
    // ============================================================

    // PRIVATE STATIC — only this class manages the counter
    // No one outside should touch the ID generator
    private static int nextId = 1;

    // PUBLIC STATIC FINAL — everyone can READ the bank name, no one can change it
    public static final String BANK_NAME = BankConfig.getInstance().getBankName();

    // PRIVATE — only this class touches these directly
    // Outside code uses getters (controlled access)
    private final String accountNumber;
    private final String ownerName;
    private final String email;
    private double balance;
    private AccountStatus status;
    private final LocalDateTime createdAt;
    // No more fixed size! No more manual counting!
    private final List<Transaction> transactions;
    private int transactionCount; // 🆕 we can still track count if we want, but it's redundant with transactions.size()
    private final AccountType accountType;

    // --- Constructors (PUBLIC — other packages need to create accounts) ---

    public BankAccount(String ownerName, String email, double initialBalance,
                       AccountType accountType) {
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("Owner name cannot be empty.");
        }
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative.");
        }
        this.accountNumber = generateAccountNumber();
        this.ownerName = ownerName;
        this.email = (email == null) ? "" : email.toLowerCase().trim();
        this.balance = initialBalance;
        this.status = AccountStatus.ACTIVE;
        this.accountType = accountType;
        this.transactions = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    public BankAccount(String ownerName, String email, double initialBalance) {
        this(ownerName, email, initialBalance, new AccountType.Savings());
    }

    public BankAccount(String ownerName, String email) {
        this(ownerName, email, 0.0, new AccountType.Savings());
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

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException(accountNumber, amount);
        }
        if (status.cannotTransact()) {
            throw new AccountFrozenException(accountNumber, status);
        }
        balance += amount;
        addTransaction(TransactionType.DEPOSIT, amount);

    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException(accountNumber, amount);
        }
        if (!status.cannotTransact()) {
            throw new AccountFrozenException(accountNumber, status);
        }
        if (!accountType.isWithinLimit(amount)) {
            throw new IllegalStateException(String.format(
                    "Withdrawal $%,.2f exceeds %s limit of $%,.2f",
                    amount, accountType.displayName(), accountType.withdrawalLimit()));
        }
        if (amount > balance) {
            throw new InsufficientFundsException(accountNumber, amount, balance);
        }
        balance -= amount;
        addTransaction(TransactionType.WITHDRAWAL, amount);
    }


    public double calculateInterest() {
        return accountType.calculateInterestRate(balance);
    }

    // 🆕 Monthly fee
    public double getMonthlyFee() {
        return accountType.monthlyFee();
    }

    // 🆕 Getter
    public AccountType getAccountType() { return accountType; }


    @Override
    public void displayInfo() {
        System.out.println("--- Account Details ---");
        System.out.println("Bank:         " + BANK_NAME);
        System.out.println("Account #:    " + accountNumber);
        System.out.println("Owner:        " + ownerName);
        System.out.println("Email:        " + email);
        System.out.println("Type:         " + accountType.displayName());
        System.out.println("Balance:      $" + String.format("%,.2f", balance));
        System.out.println("Interest:     " + String.format("%.1f%%", accountType.interestRate() * 100));
        System.out.println("Monthly fee:  $" + String.format("%.2f", accountType.monthlyFee()));
        System.out.println("Withdraw max: $" + String.format("%,.0f", accountType.withdrawalLimit()));
        System.out.println("Transactions: " + transactions.size());
        System.out.println("Status:       " + status.icon() + " " + status.description());
        System.out.println("Created:      " + getFormattedCreatedAt());
    }

    // 🆕 Update toString to show type:
    @Override
    public String toString() {
        return String.format("[%s] %s — $%,.2f %s (%s)",
                accountNumber, ownerName, balance, status.icon(),
                accountType.displayName());
    }

    // PRIVATE — internal bookkeeping, no one outside needs to call this
    private void addTransaction(TransactionType type, double amount) {
        transactions.add(new Transaction(accountNumber, type, amount, balance));
    }

    public void changeStatus(AccountStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(accountNumber, status, newStatus);
        }
        AccountStatus oldStatus = status;
        this.status = newStatus;
        System.out.println("  " + accountNumber + ": "
                + oldStatus.icon() + " " + oldStatus
                + " → " + newStatus.icon() + " " + newStatus);
    }

    public void freeze() {
        changeStatus(AccountStatus.FROZEN);
    }

    public void activate() {
        changeStatus(AccountStatus.ACTIVE);
    }

    public void close() {
        changeStatus(AccountStatus.CLOSED);
    }

    public void printMiniStatement() {
        System.out.println("--- Mini Statement for " + ownerName + " ---");

        if (transactions.isEmpty()) { // 🆕 .isEmpty() instead of count == 0
            System.out.println("  No transactions yet.");
            return;
        }

        // Show last 5 transactions
        int start = Math.max(0, transactions.size() - 5);

        // 🆕 .subList(from, to) — get a VIEW of part of the list
        // No need to manually calculate loop indices
        List<Transaction> recent = transactions.subList(start, transactions.size());
        int counter = start + 1;
        for (Transaction txn : recent) {
            System.out.println("  " + counter + ". " + txn);
            counter++;
        }
    }

    // 🆕 Get a copy of transactions (we'll improve this with Collections next)
    public List<Transaction> getTransactions() {
        return List.copyOf(transactions); // returns an unmodifiable copy of the list
    }

    public Transaction getLastTransaction() {
        if (transactions.isEmpty()) {
            return null;
        }
        return transactions.get(transactions.size() - 1);
    }

    // Iterate all transactions
    public TransactionIterator transactionIterator() {
        return new TransactionIterator(transactions);
    }

    // Iterate only a specific type
    public TransactionIterator transactionIterator(TransactionType type) {
        return new TransactionIterator(transactions, type);
    }

    // 🆕 Calculate total for a specific transaction type using our iterator
    public double getTotalByType(TransactionType type) {
        double total = 0;
        TransactionIterator it = transactionIterator(type);
        while (it.hasNext()) {
            total += it.next().getAmount();
        }
        return total;
    }

    // 🆕 Print transactions of a specific type using our iterator
    public void printTransactionsByType(TransactionType type) {
        System.out.println("--- " + type + " transactions for " + ownerName + " ---");

        TransactionIterator it = transactionIterator(type);
        int count = 0;

        while (it.hasNext()) {
            System.out.println("  " + it.next());
            count++;
        }

        if (count == 0) {
            System.out.println("  No " + type.displayName().toLowerCase() + " transactions.");
        } else {
            System.out.println("  Total " + type.displayName().toLowerCase() + ": $"
                    + String.format("%.2f", getTotalByType(type)));
        }
    }


    // --- PUBLIC getters — controlled read-only access ---

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public double getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public boolean cannotTransact() {
        return status.cannotTransact();
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public int getTransactionCount() {
        return transactions.size();
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getFormattedCreatedAt() {
        return createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
    }

    // 🆕 How old is this account?
    public String getAccountAge() {
        Period age = Period.between(createdAt.toLocalDate(), LocalDate.now());
        if (age.getYears() > 0) {
            return age.getYears() + " year(s), " + age.getMonths() + " month(s)";
        } else if (age.getMonths() > 0) {
            return age.getMonths() + " month(s), " + age.getDays() + " day(s)";
        } else if (age.getDays() > 0) {
            return age.getDays() + " day(s)";
        } else {
            return "Today";
        }
    }

    // 🆕 Get transactions from today only
    public List<Transaction> getTodaysTransactions() {
        List<Transaction> today = new ArrayList<>();
        for (Transaction txn : transactions) {
            if (txn.isToday()) {
                today.add(txn);
            }
        }
        return today;
    }

    // 🆕 Get transactions from the last N hours
    public List<Transaction> getRecentTransactions(int hours) {
        List<Transaction> recent = new ArrayList<>();
        for (Transaction txn : transactions) {
            if (txn.isWithinLastHours(hours)) {
                recent.add(txn);
            }
        }
        return recent;
    }

    // 🆕 Get total amount for a type TODAY
    public double getTodaysTotalByType(TransactionType type) {
        double total = 0;
        for (Transaction txn : transactions) {
            if (txn.isToday() && txn.getType() == type) {
                total += txn.getAmount();
            }
        }
        return total;
    }

    @Override
    public boolean equals(Object o) {
        // Step 1: Am I comparing with myself? Obviously yes.
        if (this == o) return true;

        // Step 2: Is the other thing even a BankAccount? If not, not equal.
        if (!(o instanceof BankAccount other)) return false;

        // Step 3: Same account number = same account.
        return Objects.equals(this.accountNumber, other.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }

    
}