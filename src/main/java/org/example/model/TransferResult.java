package org.example.model;

import java.time.LocalDateTime;

// ================================================
// RECORD — immutable data carrier
// ================================================
// Java auto-generates:
//   • private final fields
//   • constructor
//   • fromAccount() — getter (not getFromAccount!)
//   • toAccount()   — getter
//   • amount()      — getter
//   • etc.
//   • equals() — compares ALL fields
//   • hashCode() — based on ALL fields
//   • toString() — shows ALL fields

public record TransferResult(
        String fromAccount,
        String toAccount,
        double amount,
        double fromBalanceAfter,
        double toBalanceAfter,
        LocalDateTime timestamp,
        boolean success,
        String message
) {
    // ================================================
    // COMPACT CONSTRUCTOR — validation without parameter list
    // ================================================
    // No parentheses! No explicit assignment!
    // Java auto-assigns all fields AFTER this block runs

    public TransferResult {
        // Validation — runs before field assignment
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (fromAccount == null || fromAccount.isBlank()) {
            throw new IllegalArgumentException("Source account required");
        }
        if (toAccount == null || toAccount.isBlank()) {
            throw new IllegalArgumentException("Destination account required");
        }
        if (fromAccount.equals(toAccount)) {
            throw new IllegalArgumentException("Cannot transfer to same account");
        }

        // You can transform values in compact constructor:
        fromAccount = fromAccount.toUpperCase();
        toAccount = toAccount.toUpperCase();
        // Java then assigns: this.fromAccount = fromAccount; (auto)
    }

    // ================================================
    // STATIC FACTORY METHODS — named constructors
    // ================================================

    public static TransferResult success(String from, String to, double amount,
                                         double fromBal, double toBal) {
        return new TransferResult(from, to, amount, fromBal, toBal,
                LocalDateTime.now(), true, "Transfer successful");
    }

    public static TransferResult failure(String from, String to, double amount,
                                         String reason) {
        return new TransferResult(from, to, amount, 0, 0,
                LocalDateTime.now(), false, reason);
    }

    // ================================================
    // CUSTOM METHODS — records can have methods!
    // ================================================

    // Format for display
    public String toDisplayString() {
        if (success) {
            return String.format("✅ $%,.2f: %s → %s | Balances: $%,.2f / $%,.2f",
                    amount, fromAccount, toAccount, fromBalanceAfter, toBalanceAfter);
        } else {
            return String.format("❌ $%,.2f: %s → %s | Reason: %s",
                    amount, fromAccount, toAccount, message);
        }
    }

    // How long ago
    public String timeAgo() {
        long seconds = java.time.Duration.between(timestamp, LocalDateTime.now()).getSeconds();
        if (seconds < 60) return seconds + "s ago";
        if (seconds < 3600) return (seconds / 60) + "m ago";
        return (seconds / 3600) + "h ago";
    }
}