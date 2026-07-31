package org.example.training;

import org.example.model.AccountStatus;
import org.example.model.BankAccount;
import org.example.model.Transaction;
import org.example.model.TransactionType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StreamAccountService {

    // ================================================
    // FILTER — keep elements that match a condition
    // ================================================

    public static List<BankAccount> getActiveAccounts(List<BankAccount> accounts) {
        return accounts.stream()          // 1. create stream from list
                .filter(BankAccount::isActive)  // 2. keep only active (Predicate)
                .toList();                // 3. collect results into a new list
    }

    public static List<BankAccount> getAccountsAboveBalance(List<BankAccount> accounts,
                                                            double minBalance) {
        return accounts.stream()
                .filter(acc -> acc.getBalance() > minBalance)
                .toList();
    }

    // Chain multiple filters
    public static List<BankAccount> getActiveRichAccounts(List<BankAccount> accounts,
                                                          double minBalance) {
        return accounts.stream()
                .filter(BankAccount::isActive)               // filter 1
                .filter(acc -> acc.getBalance() > minBalance) // filter 2
                .toList();
        // Equivalent to: filter(IS_ACTIVE.and(balanceAbove(min)))
        // Stream chains read more naturally
    }

    // ================================================
    // MAP — transform each element
    // ================================================

    public static List<String> getAllNames(List<BankAccount> accounts) {
        return accounts.stream()
                .map(BankAccount::getOwnerName)  // transform Account → String
                .toList();
    }

    public static List<String> getFormattedAccounts(List<BankAccount> accounts) {
        return accounts.stream()
                .map(acc -> String.format("[%s] %s — $%,.2f %s",
                        acc.getAccountNumber(),
                        acc.getOwnerName(),
                        acc.getBalance(),
                        acc.getStatus().icon()))
                .toList();
    }

    // ================================================
    // FILTER + MAP — the classic pipeline
    // ================================================

    // Get names of all active accounts with balance > $1000
    public static List<String> getRichActiveNames(List<BankAccount> accounts) {
        return accounts.stream()
                .filter(BankAccount::isActive)
                .filter(acc -> acc.getBalance() > 1000)
                .map(BankAccount::getOwnerName)
                .toList();
    }

    // ================================================
    // SORTED — order the elements
    // ================================================

    public static List<BankAccount> sortedByBalance(List<BankAccount> accounts) {
        return accounts.stream()
                .sorted(Comparator.comparingDouble(BankAccount::getBalance))
                .toList();
    }

    public static List<BankAccount> sortedByBalanceDesc(List<BankAccount> accounts) {
        return accounts.stream()
                .sorted(Comparator.comparingDouble(BankAccount::getBalance).reversed())
                .toList();
    }

    public static List<BankAccount> sortedByName(List<BankAccount> accounts) {
        return accounts.stream()
                .sorted(Comparator.comparing(BankAccount::getOwnerName))
                .toList();
    }

    // ================================================
    // DISTINCT — remove duplicates
    // ================================================

    // Get all unique transaction types used across all accounts
    public static List<String> getUniqueTransactionTypes(List<BankAccount> accounts) {
        return accounts.stream()
                .flatMap(acc -> acc.getTransactions().stream())  // flatten all transactions
                .map(txn -> txn.getType().displayName())         // extract type name
                .distinct()                                       // remove duplicates
                .sorted()                                         // alphabetical
                .toList();
    }

    // ================================================
    // COUNT — how many match
    // ================================================

    public static long countActive(List<BankAccount> accounts) {
        return accounts.stream()
                .filter(BankAccount::isActive)
                .count();                     // terminal operation → returns long
    }

    public static long countByStatus(List<BankAccount> accounts, AccountStatus status) {
        return accounts.stream()
                .filter(acc -> acc.getStatus() == status)
                .count();
    }

    // ================================================
    // SUM / AVERAGE / MIN / MAX — numeric operations
    // ================================================

    public static double getTotalBalance(List<BankAccount> accounts) {
        return accounts.stream()
                .mapToDouble(BankAccount::getBalance)  // Stream<Account> → DoubleStream
                .sum();                                  // built-in sum
    }

    public static double getAverageBalance(List<BankAccount> accounts) {
        return accounts.stream()
                .mapToDouble(BankAccount::getBalance)
                .average()                              // returns OptionalDouble
                .orElse(0.0);                           // default if empty
    }

    public static double getMaxBalance(List<BankAccount> accounts) {
        return accounts.stream()
                .mapToDouble(BankAccount::getBalance)
                .max()
                .orElse(0.0);
    }

    public static double getMinBalance(List<BankAccount> accounts) {
        return accounts.stream()
                .mapToDouble(BankAccount::getBalance)
                .min()
                .orElse(0.0);
    }

    // ================================================
    // FIND — get the first match
    // ================================================

    public static BankAccount findRichestActive(List<BankAccount> accounts) {
        return accounts.stream()
                .filter(BankAccount::isActive)
                .max(Comparator.comparingDouble(BankAccount::getBalance))
                .orElse(null);    // returns Optional — we'll cover this in Step 27
    }

    public static BankAccount findFirstAbove(List<BankAccount> accounts, double amount) {
        return accounts.stream()
                .filter(acc -> acc.getBalance() > amount)
                .findFirst()     // returns Optional<BankAccount>
                .orElse(null);
    }

    // ================================================
    // ANYMATCH / ALLMATCH / NONEMATCH — boolean checks
    // ================================================

    public static boolean anyAccountAbove(List<BankAccount> accounts, double amount) {
        return accounts.stream()
                .anyMatch(acc -> acc.getBalance() > amount);
        // true if ANY account has balance > amount
    }

    public static boolean allAccountsActive(List<BankAccount> accounts) {
        return accounts.stream()
                .allMatch(BankAccount::isActive);
        // true only if ALL accounts are active
    }

    public static boolean noFrozenAccounts(List<BankAccount> accounts) {
        return accounts.stream()
                .noneMatch(acc -> acc.getStatus() == AccountStatus.FROZEN);
        // true only if ZERO accounts are frozen
    }

    // ================================================
    // REDUCE — combine all elements into one result
    // ================================================

    // Sum all balances using reduce (alternative to mapToDouble.sum)
    public static double totalBalanceReduce(List<BankAccount> accounts) {
        return accounts.stream()
                .map(BankAccount::getBalance)
                .reduce(0.0, Double::sum);
        //        ↑ identity   ↑ accumulator
        // Start at 0.0, add each balance to the running total
    }

    // Concatenate all account names
    public static String allNamesJoined(List<BankAccount> accounts) {
        return accounts.stream()
                .map(BankAccount::getOwnerName)
                .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
    }

    // ================================================
    // COLLECT — gather results into a collection
    // ================================================

    public static List<BankAccount> getActiveAccountsMutable(List<BankAccount> accounts) {
        return accounts.stream()
                .filter(BankAccount::isActive)
                .collect(Collectors.toList());  // mutable ArrayList (vs immutable .toList())
    }

    // Joining strings
    public static String getAccountNamesCSV(List<BankAccount> accounts) {
        return accounts.stream()
                .map(BankAccount::getOwnerName)
                .collect(Collectors.joining(", "));
        // "Alice, Bob, Carol"
    }

    // ================================================
    // FLATMAP — flatten nested structures
    // ================================================

    // Get ALL transactions from ALL accounts as one flat list
    public static List<Transaction> getAllTransactions(List<BankAccount> accounts) {
        return accounts.stream()
                .flatMap(acc -> acc.getTransactions().stream())
                // Each account has a List<Transaction>
                // flatMap turns Stream<List<Transaction>> into Stream<Transaction>
                // Think: "unwrap each inner list and merge them all"
                .toList();
    }

    // Get all deposit amounts across all accounts
    public static List<Double> getAllDepositAmounts(List<BankAccount> accounts) {
        return accounts.stream()
                .flatMap(acc -> acc.getTransactions().stream())  // flatten
                .filter(txn -> txn.getType() == TransactionType.DEPOSIT)  // only deposits
                .map(Transaction::getAmount)                     // extract amount
                .toList();
    }

    // ================================================
    // SUMMARY STATISTICS — everything at once
    // ================================================

    public static DoubleSummaryStatistics getBalanceStats(List<BankAccount> accounts) {
        return accounts.stream()
                .mapToDouble(BankAccount::getBalance)
                .summaryStatistics();
        // Returns: count, sum, min, max, average — all in one pass!
    }

    // ================================================
    // 🆕 PRACTICAL: Full bank dashboard using streams
    // ================================================

    public static void printDashboard(List<BankAccount> accounts) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║          SMARTBANK STREAM DASHBOARD          ║");
        System.out.println("╠══════════════════════════════════════════════╣");

        if (accounts.isEmpty()) {
            System.out.println("║  No accounts.");
            System.out.println("╚══════════════════════════════════════════════╝");
            return;
        }

        // --- Account statistics ---
        long total = accounts.size();
        long active = countActive(accounts);
        long frozen = countByStatus(accounts, AccountStatus.FROZEN);
        long closed = countByStatus(accounts, AccountStatus.CLOSED);

        System.out.printf("║  Accounts: %d total | %d active | %d frozen | %d closed%n",
                total, active, frozen, closed);

        // --- Balance statistics (all in one pass!) ---
        DoubleSummaryStatistics stats = getBalanceStats(accounts);
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.printf("║  Total balance:   $%,.2f%n", stats.getSum());
        System.out.printf("║  Average balance: $%,.2f%n", stats.getAverage());
        System.out.printf("║  Highest balance: $%,.2f%n", stats.getMax());
        System.out.printf("║  Lowest balance:  $%,.2f%n", stats.getMin());

        // --- Top 3 richest ---
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║  🏆 Top 3 Richest:");

        accounts.stream()
                .sorted(Comparator.comparingDouble(BankAccount::getBalance).reversed())
                .limit(3)
                .forEach(acc -> System.out.printf("║    %s %s — $%,.2f%n",
                        acc.getStatus().icon(),
                        acc.getOwnerName(),
                        acc.getBalance()));

        // --- All unique transaction types ---
        List<String> types = getUniqueTransactionTypes(accounts);
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║  Transaction types seen: " + String.join(", ", types));

        // --- Total transactions across all accounts ---
        long totalTxns = accounts.stream()
                .mapToInt(BankAccount::getTransactionCount)
                .sum();
        System.out.println("║  Total transactions: " + totalTxns);

        // --- Boolean checks ---
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║  Any millionaire?  "
                + (anyAccountAbove(accounts, 1_000_000) ? "Yes 💰" : "No"));
        System.out.println("║  All active?       "
                + (allAccountsActive(accounts) ? "Yes ✅" : "No ❌"));
        System.out.println("║  Any frozen?       "
                + (noFrozenAccounts(accounts) ? "No ✅" : "Yes 🧊"));

        // --- Account list sorted by balance ---
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║  All accounts (by balance descending):");

        accounts.stream()
                .sorted(Comparator.comparingDouble(BankAccount::getBalance).reversed())
                .map(acc -> String.format("║    %s [%s] %-18s $%,.2f  (%d txns)",
                        acc.getStatus().icon(),
                        acc.getAccountNumber(),
                        acc.getOwnerName(),
                        acc.getBalance(),
                        acc.getTransactionCount()))
                .forEach(System.out::println);

        System.out.println("╚══════════════════════════════════════════════╝");
    }

    // ================================================
    // 🆕 TRANSACTION ANALYSIS — streams on transactions
    // ================================================

    public static void printTransactionAnalysis(BankAccount account) {
        List<Transaction> txns = account.getTransactions();

        System.out.println();
        System.out.println("--- Transaction Analysis: " + account.getOwnerName() + " ---");

        if (txns.isEmpty()) {
            System.out.println("  No transactions.");
            return;
        }

        // Count by type
        System.out.println("  By type:");
        long deposits = txns.stream()
                .filter(t -> t.getType() == TransactionType.DEPOSIT)
                .count();
        long withdrawals = txns.stream()
                .filter(t -> t.getType() == TransactionType.WITHDRAWAL)
                .count();
        System.out.println("    Deposits:    " + deposits);
        System.out.println("    Withdrawals: " + withdrawals);

        // Sum by type
        double depositTotal = txns.stream()
                .filter(t -> t.getType() == TransactionType.DEPOSIT)
                .mapToDouble(Transaction::getAmount)
                .sum();
        double withdrawalTotal = txns.stream()
                .filter(t -> t.getType() == TransactionType.WITHDRAWAL)
                .mapToDouble(Transaction::getAmount)
                .sum();
        System.out.printf("    Deposit total:    $%,.2f%n", depositTotal);
        System.out.printf("    Withdrawal total: $%,.2f%n", withdrawalTotal);
        System.out.printf("    Net:              $%,.2f%n", depositTotal - withdrawalTotal);

        // Largest transaction
        txns.stream()
                .max(Comparator.comparingDouble(Transaction::getAmount))
                .ifPresent(t -> System.out.println("  Largest: "
                        + t.getType().formatAmount(t.getAmount())
                        + " " + t.getType().displayName()));

        // Most recent 3
        System.out.println("  Most recent 3:");
        txns.stream()
                .sorted(Comparator.comparing(Transaction::getDateTime).reversed())
                .limit(3)
                .forEach(t -> System.out.println("    " + t.getShortDateTime()
                        + " " + t.getType().formatAmount(t.getAmount())
                        + " " + t.getType().displayName()));

        // Today's activity
        long todayCount = txns.stream()
                .filter(Transaction::isToday)
                .count();
        double todayTotal = txns.stream()
                .filter(Transaction::isToday)
                .mapToDouble(Transaction::getAmount)
                .sum();
        System.out.printf("  Today: %d transactions, $%,.2f total%n", todayCount, todayTotal);
    }
}