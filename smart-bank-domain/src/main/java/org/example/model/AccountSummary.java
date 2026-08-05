package org.example.model;

// ================================================
// RECORD AS A SNAPSHOT — freezes account state at a point in time
// ================================================
// Unlike BankAccount (which changes), this is a frozen picture

public record AccountSummary(
        String accountNumber,
        String ownerName,
        double balance,
        AccountStatus status,
        int transactionCount,
        String statusIcon
) {
    // ================================================
    // FACTORY METHOD — create from a live BankAccount
    // ================================================
    // This captures the current state — like taking a photograph

    public static AccountSummary from(BankAccount account) {
        return new AccountSummary(
                account.getAccountNumber(),
                account.getOwnerName(),
                account.getBalance(),
                account.getStatus(),
                account.getTransactionCount(),
                account.getStatus().icon()
        );
    }

    // ================================================
    // RECORDS IMPLEMENT INTERFACES
    // ================================================
    // Records can implement interfaces but NOT extend classes
    // (they implicitly extend java.lang.Record)

    // Custom display
    public String toDisplayLine() {
        return String.format("%s [%s] %-20s $%,.2f (%d txns)",
                statusIcon, accountNumber, ownerName, balance, transactionCount);
    }

    // Check if "rich"
    public boolean isRich(double threshold) {
        return balance >= threshold;
    }
}