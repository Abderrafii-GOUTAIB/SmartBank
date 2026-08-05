package org.example.model;

import java.time.LocalDateTime;

// ================================================
// SEALED CLASS — all bank events must be one of these types
// ================================================

public sealed abstract class BankEvent
        permits BankEvent.AccountEvent, BankEvent.TransactionEvent, BankEvent.SystemEvent {

    private final LocalDateTime timestamp;
    private final String description;

    protected BankEvent(String description) {
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDescription() { return description; }

    public abstract String getEventType();

    @Override
    public String toString() {
        return String.format("[%s] %s: %s",
                timestamp.format(Transaction.SHORT_FORMAT),
                getEventType(),
                description);
    }

    // ================================================
    // LEVEL 2: AccountEvent is also SEALED — controls its subtypes
    // ================================================

    public static sealed abstract class AccountEvent extends BankEvent
            permits AccountEvent.AccountCreated,
            AccountEvent.AccountClosed,
            AccountEvent.StatusChanged {

        private final String accountNumber;

        protected AccountEvent(String accountNumber, String description) {
            super(description);
            this.accountNumber = accountNumber;
        }

        public String getAccountNumber() { return accountNumber; }

        // --- FINAL subclasses — the chain ends here ---

        public static final class AccountCreated extends AccountEvent {
            private final String ownerName;

            public AccountCreated(String accountNumber, String ownerName) {
                super(accountNumber, "Account created for " + ownerName);
                this.ownerName = ownerName;
            }

            public String getOwnerName() { return ownerName; }
            @Override public String getEventType() { return "ACCOUNT_CREATED"; }
        }

        public static final class AccountClosed extends AccountEvent {
            public AccountClosed(String accountNumber) {
                super(accountNumber, "Account closed");
            }

            @Override public String getEventType() { return "ACCOUNT_CLOSED"; }
        }

        public static final class StatusChanged extends AccountEvent {
            private final AccountStatus from;
            private final AccountStatus to;

            public StatusChanged(String accountNumber, AccountStatus from, AccountStatus to) {
                super(accountNumber,
                        "Status: " + from.icon() + " " + from + " → " + to.icon() + " " + to);
                this.from = from;
                this.to = to;
            }

            public AccountStatus getFrom() { return from; }
            public AccountStatus getTo() { return to; }
            @Override public String getEventType() { return "STATUS_CHANGED"; }
        }
    }

    // ================================================
    // LEVEL 2: TransactionEvent is FINAL — no further subtypes
    // ================================================

    public static final class TransactionEvent extends BankEvent {
        private final String accountNumber;
        private final TransactionType type;
        private final double amount;

        public TransactionEvent(String accountNumber, TransactionType type, double amount) {
            super(type.displayName() + " of " + type.formatAmount(amount)
                    + " on " + accountNumber);
            this.accountNumber = accountNumber;
            this.type = type;
            this.amount = amount;
        }

        public String getAccountNumber() { return accountNumber; }
        public TransactionType getType() { return type; }
        public double getAmount() { return amount; }
        @Override public String getEventType() { return "TRANSACTION"; }
    }

    // ================================================
    // LEVEL 2: SystemEvent is NON-SEALED — anyone can extend
    // ================================================

    public static non-sealed class SystemEvent extends BankEvent {
        public SystemEvent(String description) {
            super(description);
        }

        @Override public String getEventType() { return "SYSTEM"; }
    }
}