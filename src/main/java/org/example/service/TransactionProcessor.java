package org.example.service;

import org.example.model.BankAccount;
import org.example.model.Transaction;
import org.example.model.TransactionType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TransactionProcessor {

    private TransactionProcessor() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    // ================================================
    // INTERFACE — defines what a "filter" does
    // ================================================

    // This interface has ONE method — it's a "functional interface"
    // That matters in Step 22 (lambdas can replace it)
    public interface TransactionFilter {
        boolean accept(Transaction transaction);
    }

    // This interface has ONE method — also functional
    public interface TransactionTransformer {
        String transform(Transaction transaction);
    }

    // This interface has MULTIPLE methods — NOT functional
    // Lambdas CANNOT replace this — anonymous class is required
    public interface TransactionHandler {
        void onStart(String accountName);
        void onTransaction(Transaction transaction);
        void onComplete(int totalProcessed, double totalAmount);
    }

    // ================================================
    // ANONYMOUS CLASS EXAMPLE 1 — Single-method interface
    // (These will become lambdas in Step 22)
    // ================================================

    public static List<Transaction> filterTransactions(List<Transaction> transactions,
                                                       TransactionFilter filter) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction txn : transactions) {
            if (filter.accept(txn)) {
                result.add(txn);
            }
        }
        return result;
    }

    // Predefined filters using anonymous classes
    // Each one creates a nameless class that implements TransactionFilter

    // Filter: only deposits
    public static final TransactionFilter DEPOSITS_ONLY = new TransactionFilter() {
        @Override
        public boolean accept(Transaction transaction) {
            return transaction.getType() == TransactionType.DEPOSIT;
        }
    };

    // Filter: only withdrawals
    public static final TransactionFilter WITHDRAWALS_ONLY = new TransactionFilter() {
        @Override
        public boolean accept(Transaction transaction) {
            return transaction.getType() == TransactionType.WITHDRAWAL;
        }
    };

    // Filter: large transactions (above a threshold)
    // This shows that anonymous classes can capture variables
    public static TransactionFilter aboveAmount(double threshold) {
        return new TransactionFilter() {
            @Override
            public boolean accept(Transaction transaction) {
                return transaction.getAmount() > threshold;
            }
        };
    }

    // ================================================
    // ANONYMOUS CLASS EXAMPLE 2 — Transformer
    // ================================================

    public static List<String> transformTransactions(List<Transaction> transactions,
                                                     TransactionTransformer transformer) {
        List<String> result = new ArrayList<>();
        for (Transaction txn : transactions) {
            result.add(transformer.transform(txn));
        }
        return result;
    }

    // Receipt-style transformer
    public static final TransactionTransformer RECEIPT_FORMAT = new TransactionTransformer() {
        @Override
        public String transform(Transaction transaction) {
            return String.format("  [%s] %s %s",
                    transaction.getType().displayName(),
                    transaction.getType().formatAmount(transaction.getAmount()),
                    transaction.getShortDateTime());
        }
    };

    // CSV transformer — for export
    public static final TransactionTransformer CSV_FORMAT = new TransactionTransformer() {
        @Override
        public String transform(Transaction transaction) {
            return String.join(",",
                    String.valueOf(transaction.getId()),
                    transaction.getType().name(),
                    String.valueOf(transaction.getAmount()),
                    String.valueOf(transaction.getBalanceAfter()),
                    transaction.getFileDateTime());
        }
    };

    // ================================================
    // ANONYMOUS CLASS EXAMPLE 3 — Multi-method interface
    // This CANNOT be replaced by a lambda (multiple methods)
    // ================================================

    public static void processAll(BankAccount account, TransactionHandler handler) {
        List<Transaction> transactions = account.getTransactions();

        handler.onStart(account.getOwnerName());

        double totalAmount = 0;
        int count = 0;

        for (Transaction txn : transactions) {
            handler.onTransaction(txn);
            totalAmount += txn.getAmount();
            count++;
        }

        handler.onComplete(count, totalAmount);
    }

    // A "console printer" handler — anonymous class with 3 methods
    public static final TransactionHandler CONSOLE_HANDLER = new TransactionHandler() {
        @Override
        public void onStart(String accountName) {
            System.out.println("╔══════════════════════════════════════╗");
            System.out.println("║  Processing: " + accountName);
            System.out.println("╠══════════════════════════════════════╣");
        }

        @Override
        public void onTransaction(Transaction transaction) {
            System.out.println("║  " + transaction.getType().formatAmount(transaction.getAmount())
                    + "  " + transaction.getType().displayName()
                    + "  " + transaction.getShortDateTime());
        }

        @Override
        public void onComplete(int totalProcessed, double totalAmount) {
            System.out.println("╠══════════════════════════════════════╣");
            System.out.printf("║  Total: %d transactions, $%.2f%n", totalProcessed, totalAmount);
            System.out.println("╚══════════════════════════════════════╝");
        }
    };

    // A "summary only" handler — different behavior, same interface
    public static final TransactionHandler SUMMARY_HANDLER = new TransactionHandler() {
        // This handler has STATE — a field that tracks across calls
        // Lambdas can't have fields — another reason anonymous classes exist
        private int depositCount = 0;
        private int withdrawalCount = 0;

        @Override
        public void onStart(String accountName) {
            depositCount = 0;
            withdrawalCount = 0;
            System.out.println("--- Summary for " + accountName + " ---");
        }

        @Override
        public void onTransaction(Transaction transaction) {
            // Silently count — don't print each one
            if (transaction.getType().isCredit()) depositCount++;
            else withdrawalCount++;
        }

        @Override
        public void onComplete(int totalProcessed, double totalAmount) {
            System.out.println("  Deposits:    " + depositCount);
            System.out.println("  Withdrawals: " + withdrawalCount);
            System.out.printf("  Total value:  $%.2f%n", totalAmount);
        }
    };

    // ================================================
    // ANONYMOUS CLASS EXAMPLE 4 — Custom Comparator inline
    // ================================================

    public static List<Transaction> sortByAmountDescending(List<Transaction> transactions) {
        List<Transaction> sorted = new ArrayList<>(transactions);

        // Anonymous Comparator — defined right where it's used
        sorted.sort(new Comparator<Transaction>() {
            @Override
            public int compare(Transaction a, Transaction b) {
                // Reversed: b compared to a = descending
                return Double.compare(b.getAmount(), a.getAmount());
            }
        });

        return sorted;
    }

    // ================================================
    // COMBINING FILTERS — anonymous class that wraps two others
    // ================================================

    // Combine two filters with AND logic
    public static TransactionFilter and(TransactionFilter filter1, TransactionFilter filter2) {
        return new TransactionFilter() {
            @Override
            public boolean accept(Transaction transaction) {
                return filter1.accept(transaction) && filter2.accept(transaction);
            }
        };
    }

    // Combine two filters with OR logic
    public static TransactionFilter or(TransactionFilter filter1, TransactionFilter filter2) {
        return new TransactionFilter() {
            @Override
            public boolean accept(Transaction transaction) {
                return filter1.accept(transaction) || filter2.accept(transaction);
            }
        };
    }

    // Negate a filter
    public static TransactionFilter not(TransactionFilter filter) {
        return new TransactionFilter() {
            @Override
            public boolean accept(Transaction transaction) {
                return !filter.accept(transaction);
            }
        };
    }
}