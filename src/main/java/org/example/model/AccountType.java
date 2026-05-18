package org.example.model;

public sealed interface AccountType permits AccountType.Savings, AccountType.Checking, AccountType.Business, AccountType.Investment {
    String displayName();

    double interestRate();

    double monthlyFee();

    double withdrawalLimit();

    default String summary() {
        return String.format("%s — %.1f%% interest, $%.2f/month fee, $%,.0f withdrawal limit",
                displayName(), interestRate() * 100, monthlyFee(), withdrawalLimit());
    }

    default double calculateInterestRate(double balance) {
        return balance * interestRate();
    }

    default boolean isWithinLimit(double amount) {
        return amount <= withdrawalLimit();
    }

    record Savings(double interestRate) implements AccountType {
        public Savings {
            if (interestRate < 0 || interestRate > 0.5) {
                throw new IllegalArgumentException("Interest rate must be 0-50%");
            }
        }

        // Default savings parameters
        public Savings() {
            this(0.045);
        } // 4.5% default

        @Override
        public String displayName() {
            return "Savings";
        }

        @Override
        public double monthlyFee() {
            return 0;
        }

        @Override
        public double withdrawalLimit() {
            return 10_000;
        }
    }

    record Checking(double monthlyFee) implements AccountType {
        public Checking() {
            this(4.99);
        }

        @Override
        public String displayName() {
            return "Checking";
        }

        @Override
        public double interestRate() {
            return 0.001;
        } // 0.1%

        @Override
        public double withdrawalLimit() {
            return 50_000;
        }

    }

    record Business(double interestRate, double monthlyFee) implements AccountType {
        public Business() {
            this(0.02, 29.99);
        } // 2%, $29.99/month

        @Override
        public String displayName() {
            return "Business";
        }

        @Override
        public double withdrawalLimit() {
            return 500_000;
        }
    }

    record Investment(double interestRate, double monthlyFee) implements AccountType {
        public Investment() {
            this(0.035, 49.99);
        }

        @Override
        public String displayName() {
            return "Investment";
        }

        @Override
        public double withdrawalLimit() {
            return 100_000;
        }

        public int lockPeriodMonths() {
            return 12;
        }
    }
}
