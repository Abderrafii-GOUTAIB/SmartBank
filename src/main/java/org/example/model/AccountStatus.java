package org.example.model;

public enum AccountStatus {

    PENDING("Pending — awaiting verification") {
        @Override
        public boolean canTransitionTo(AccountStatus next) {
            return next == ACTIVE || next == REJECTED;
        }
    },

    ACTIVE("Active — fully operational") {
        @Override
        public boolean canTransitionTo(AccountStatus next) {
            return next == FROZEN || next == CLOSED;
        }
    },

    FROZEN("Frozen — temporarily restricted") {
        @Override
        public boolean canTransitionTo(AccountStatus next) {
            return next == ACTIVE || next == CLOSED;
        }
    },

    REJECTED("Rejected — application denied") {
        @Override
        public boolean canTransitionTo(AccountStatus next) {
            return false; // terminal state — can't go anywhere
        }
    },

    CLOSED("Closed — permanently shut down") {
        @Override
        public boolean canTransitionTo(AccountStatus next) {
            return false; // terminal state
        }
    };

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }

    public String description() { return description; }

    // Abstract method — each constant MUST implement this
    public abstract boolean canTransitionTo(AccountStatus next);

    // Check if this is a terminal (final) state
    public boolean isTerminal() {
        // If you can't transition to ANY status, you're terminal
        for (AccountStatus status : values()) {
            if (canTransitionTo(status)) return false;
        }
        return true;
    }

    // Check if the account can perform transactions
    public boolean cannotTransact() {
        return this != ACTIVE;
    }

    // Emoji for display
    public String icon() {
        return switch (this) {
            case PENDING  -> "⏳";
            case ACTIVE   -> "✅";
            case FROZEN   -> "🧊";
            case REJECTED -> "❌";
            case CLOSED   -> "🔒";
        };
    }
}