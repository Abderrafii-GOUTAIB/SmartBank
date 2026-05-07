package org.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ================================================
// RULE 1: Class is FINAL — nobody can extend and add mutable fields
// ================================================
public final class ImmutableAccount {

    // ================================================
    // RULE 2: ALL fields are PRIVATE and FINAL
    // ================================================
    private final String accountNumber;
    private final String ownerName;
    private final String email;
    private final double balance;
    private final AccountStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime snapshotAt;

    // ================================================
    // RULE 3: Collection fields store IMMUTABLE copies
    // ================================================
    private final List<Transaction> transactions;
    // List.copyOf() in constructor — immutable list
    // Transaction has no setters and all final fields — immutable objects

    // ================================================
    // RULE 4: Constructor makes DEFENSIVE COPIES of all mutable inputs
    // ================================================
    public ImmutableAccount(String accountNumber, String ownerName, String email,
                            double balance, AccountStatus status,
                            LocalDateTime createdAt,
                            List<Transaction> transactions) {
        // Validate — immutable objects should be valid from birth
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Account number required");
        }
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("Owner name required");
        }

        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.email = email != null ? email.toLowerCase().trim() : "";
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt; // LocalDateTime is already immutable ✅

        // DEFENSIVE COPY — even if caller modifies their list later, we're safe
        this.transactions = List.copyOf(transactions);
        // List.copyOf creates an unmodifiable copy
        // AND Transaction objects are immutable (all final fields, no setters)
        // So this is DEEP immutability — level 3 protection ✅

        this.snapshotAt = LocalDateTime.now();
    }

    // ================================================
    // RULE 5: FACTORY METHOD — create from live BankAccount
    // ================================================
    public static ImmutableAccount snapshotOf(BankAccount account) {
        return new ImmutableAccount(
                account.getAccountNumber(),
                account.getOwnerName(),
                account.getEmail(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getTransactions() // already returns List.copyOf
        );
    }

    // ================================================
    // RULE 6: NO SETTERS — only getters
    // ================================================
    // The ONLY way to "change" an immutable object is to create a NEW one

    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName()     { return ownerName; }
    public String getEmail()         { return email; }
    public double getBalance()       { return balance; }
    public AccountStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getSnapshotAt()  { return snapshotAt; }

    // Returns the SAME immutable list — safe to share, no copy needed
    public List<Transaction> getTransactions() { return transactions; }

    public int getTransactionCount() { return transactions.size(); }

    // ================================================
    // RULE 7: "Modification" returns a NEW object (copy-on-write)
    // ================================================
    // Like String: "hello".toUpperCase() returns NEW string, original unchanged

    public ImmutableAccount withBalance(double newBalance) {
        // Returns a NEW ImmutableAccount — 'this' is unchanged
        return new ImmutableAccount(
                accountNumber, ownerName, email,
                newBalance, status, createdAt, transactions);
    }

    public ImmutableAccount withStatus(AccountStatus newStatus) {
        return new ImmutableAccount(
                accountNumber, ownerName, email,
                balance, newStatus, createdAt, transactions);
    }

    public ImmutableAccount withAddedTransaction(Transaction txn) {
        // Create new list with the added transaction
        var newTxns = new ArrayList<>(transactions);
        newTxns.add(txn);
        return new ImmutableAccount(
                accountNumber, ownerName, email,
                balance, status, createdAt, newTxns);
    }

    // ================================================
    // Display
    // ================================================

    @Override
    public String toString() {
        return String.format("[SNAPSHOT %s] [%s] %s — $%,.2f %s (%d txns) — taken at %s",
                snapshotAt.format(Transaction.SHORT_FORMAT),
                accountNumber, ownerName, balance,
                status.icon(), transactions.size(),
                snapshotAt.format(Transaction.DISPLAY_FORMAT));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableAccount that)) return false;
        return Double.compare(that.balance, balance) == 0
                && java.util.Objects.equals(accountNumber, that.accountNumber)
                && java.util.Objects.equals(ownerName, that.ownerName)
                && status == that.status;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(accountNumber, ownerName, balance, status);
    }
}