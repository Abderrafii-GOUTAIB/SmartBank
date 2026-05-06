package org.example.model;

import java.time.LocalDateTime;

// ================================================
// SEALED INTERFACE — a result is EITHER Success OR Failure
// No other possibilities exist — the compiler knows this
// ================================================

public sealed interface TransactionResult
        permits TransactionResult.Success, TransactionResult.Failure {

    // Common data
    String accountNumber();
    double amount();
    LocalDateTime timestamp();

    // Common methods
    boolean isSuccess();
    String message();

    // ================================================
    // SUCCESS — carries the result data
    // ================================================

    record Success(
            String accountNumber,
            double amount,
            double balanceAfter,
            TransactionType type,
            LocalDateTime timestamp
    ) implements TransactionResult {

        public Success(String accountNumber, double amount,
                       double balanceAfter, TransactionType type) {
            this(accountNumber, amount, balanceAfter, type, LocalDateTime.now());
        }

        @Override
        public boolean isSuccess() { return true; }

        @Override
        public String message() {
            return String.format("✅ %s of %s successful. Balance: $%,.2f",
                    type.displayName(),
                    type.formatAmount(amount),
                    balanceAfter);
        }
    }

    // ================================================
    // FAILURE — carries the error information
    // ================================================

    record Failure(
            String accountNumber,
            double amount,
            String errorCode,
            String reason,
            LocalDateTime timestamp
    ) implements TransactionResult {

        public Failure(String accountNumber, double amount,
                       String errorCode, String reason) {
            this(accountNumber, amount, errorCode, reason, LocalDateTime.now());
        }

        @Override
        public boolean isSuccess() { return false; }

        @Override
        public String message() {
            return String.format("❌ [%s] %s", errorCode, reason);
        }
    }
}