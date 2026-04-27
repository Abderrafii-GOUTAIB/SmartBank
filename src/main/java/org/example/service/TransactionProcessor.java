package org.example.service;

import org.example.model.BankAccount;
import org.example.model.Transaction;
import org.example.model.TransactionType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

public class TransactionProcessor {

    // ================================================
    // INTERFACES (unchanged from Step 21)
    // ================================================

    @FunctionalInterface  // 🆕 This annotation ENFORCES single-method rule
    public interface TransactionFilter {
        boolean accept(Transaction transaction);
    }

    @FunctionalInterface
    public interface TransactionTransformer {
        String transform(Transaction transaction);
    }

    // NOT @FunctionalInterface — has multiple methods, lambdas can't touch this
    public interface TransactionHandler {
        void onStart(String accountName);
        void onTransaction(Transaction transaction);
        void onComplete(int totalProcessed, double totalAmount);
    }

    // ================================================
    // CORE METHODS (unchanged)
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

    public static List<String> transformTransactions(List<Transaction> transactions,
                                                     TransactionTransformer transformer) {
        List<String> result = new ArrayList<>();
        for (Transaction txn : transactions) {
            result.add(transformer.transform(txn));
        }
        return result;
    }

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

    // ================================================
    // 🆕 LAMBDA FILTERS — replacing anonymous classes
    // ================================================

    // --- BEFORE (anonymous class from Step 21): ---
    // public static final TransactionFilter DEPOSITS_ONLY = new TransactionFilter() {
    //     @Override
    //     public boolean accept(Transaction transaction) {
    //         return transaction.getType() == TransactionType.DEPOSIT;
    //     }
    // };

    // --- AFTER (lambda — same behavior, 1 line): ---
    public static final TransactionFilter DEPOSITS_ONLY =
            txn -> txn.getType() == TransactionType.DEPOSIT;

    public static final TransactionFilter WITHDRAWALS_ONLY =
            txn -> txn.getType() == TransactionType.WITHDRAWAL;

    public static final TransactionFilter CREDITS_ONLY =
            txn -> txn.getType().isCredit();

    public static final TransactionFilter DEBITS_ONLY =
            txn -> txn.getType().isDebit();

    // 🆕 Lambda with captured variable (closure)
    // --- BEFORE: ---
    // public static TransactionFilter aboveAmount(double threshold) {
    //     return new TransactionFilter() {
    //         @Override
    //         public boolean accept(Transaction transaction) {
    //             return transaction.getAmount() > threshold;
    //         }
    //     };
    // }

    // --- AFTER: ---
    public static TransactionFilter aboveAmount(double threshold) {
        // 'threshold' is effectively final — captured by the lambda
        return txn -> txn.getAmount() > threshold;
    }

    public static TransactionFilter belowAmount(double threshold) {
        return txn -> txn.getAmount() < threshold;
    }

    public static TransactionFilter betweenAmount(double min, double max) {
        // Lambda captures BOTH variables — both must be effectively final
        return txn -> txn.getAmount() >= min && txn.getAmount() <= max;
    }

    public static TransactionFilter ofType(TransactionType type) {
        return txn -> txn.getType() == type;
    }

    // 🆕 Today's transactions only
    public static final TransactionFilter TODAY_ONLY =
            txn -> txn.isToday();

    // ================================================
    // 🆕 LAMBDA TRANSFORMERS
    // ================================================

    // --- BEFORE (anonymous class): ---
    // public static final TransactionTransformer RECEIPT_FORMAT = new TransactionTransformer() {
    //     @Override
    //     public String transform(Transaction transaction) {
    //         return String.format("  [%s] %s %s", ...);
    //     }
    // };

    // --- AFTER (lambda): ---
    public static final TransactionTransformer RECEIPT_FORMAT =
            txn -> String.format("  [%-12s] %s  %s",
                    txn.getType().displayName(),
                    txn.getType().formatAmount(txn.getAmount()),
                    txn.getShortDateTime());

    public static final TransactionTransformer CSV_FORMAT =
            txn -> String.join(",",
                    String.valueOf(txn.getId()),
                    txn.getType().name(),
                    String.valueOf(txn.getAmount()),
                    String.valueOf(txn.getBalanceAfter()),
                    txn.getFileDateTime());

    // 🆕 Multi-line lambda (when logic is more complex)
    public static final TransactionTransformer SUMMARY_FORMAT = txn -> {
        String direction = txn.getType().isCredit() ? "IN " : "OUT";
        String amount = String.format("$%.2f", txn.getAmount());
        String timeAgo = txn.getTimeAgo();
        return String.format("  %s  %-10s  %s", direction, amount, timeAgo);
    };

    // ================================================
    // 🆕 LAMBDA COMPARATORS — replacing anonymous Comparator classes
    // ================================================

    // --- BEFORE: ---
    // sorted.sort(new Comparator<Transaction>() {
    //     @Override
    //     public int compare(Transaction a, Transaction b) {
    //         return Double.compare(b.getAmount(), a.getAmount());
    //     }
    // });

    // --- AFTER: ---
    public static List<Transaction> sortByAmountDescending(List<Transaction> transactions) {
        List<Transaction> sorted = new ArrayList<>(transactions);
        sorted.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
        return sorted;
    }

    public static List<Transaction> sortByDateDescending(List<Transaction> transactions) {
        List<Transaction> sorted = new ArrayList<>(transactions);
        sorted.sort((a, b) -> b.getDateTime().compareTo(a.getDateTime()));
        return sorted;
    }

    // ================================================
    // 🆕 FILTER COMBINATORS — now with lambdas
    // ================================================

    // --- BEFORE (anonymous class): ---
    // public static TransactionFilter and(TransactionFilter f1, TransactionFilter f2) {
    //     return new TransactionFilter() {
    //         @Override
    //         public boolean accept(Transaction transaction) {
    //             return f1.accept(transaction) && f2.accept(transaction);
    //         }
    //     };
    // }

    // --- AFTER (lambda): ---
    public static TransactionFilter and(TransactionFilter f1, TransactionFilter f2) {
        return txn -> f1.accept(txn) && f2.accept(txn);
    }

    public static TransactionFilter or(TransactionFilter f1, TransactionFilter f2) {
        return txn -> f1.accept(txn) || f2.accept(txn);
    }

    public static TransactionFilter not(TransactionFilter filter) {
        return txn -> !filter.accept(txn);
    }

    // ================================================
    // 🆕 MULTI-METHOD HANDLERS — these STILL need anonymous classes
    // Lambdas CANNOT replace these (multiple methods)
    // ================================================

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

    public static final TransactionHandler SUMMARY_HANDLER = new TransactionHandler() {
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
    // 🆕 HIGHER-ORDER METHODS — methods that ACCEPT lambdas
    // ================================================

    // Process each transaction with a custom action (lambda as parameter)
    public static void forEach(List<Transaction> transactions,
                               Consumer<Transaction> action) {
        for (Transaction txn : transactions) {
            action.accept(txn);
        }
    }

    // Calculate a value from each transaction (lambda as parameter)
    public static double sumBy(List<Transaction> transactions,
                               ToDoubleFunction<Transaction> extractor) {
        double total = 0;
        for (Transaction txn : transactions) {
            total += extractor.applyAsDouble(txn);
        }
        return total;
    }

    // Find first transaction matching a condition
    public static Transaction findFirst(List<Transaction> transactions,
                                        TransactionFilter filter) {
        for (Transaction txn : transactions) {
            if (filter.accept(txn)) return txn;
        }
        return null;
    }

    // Count transactions matching a condition
    public static int count(List<Transaction> transactions,
                            TransactionFilter filter) {
        int count = 0;
        for (Transaction txn : transactions) {
            if (filter.accept(txn)) count++;
        }
        return count;
    }
}