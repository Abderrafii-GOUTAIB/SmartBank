package org.example.service;

import org.example.model.AccountStatus;
import org.example.model.BankAccount;
import org.example.model.Transaction;
import org.example.model.TransactionType;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StreamAdvancedService {

    public static Map<AccountStatus, List<BankAccount>> groupByStatus(List<BankAccount> accounts) {
        return accounts.stream().collect(Collectors.groupingBy(BankAccount::getStatus));
        // { ACTIVE: [Alice, Bob], FROZEN: [Carol], CLOSED: [Dave] }
    }

    public static Map<AccountStatus, Long> countByStatus(List<BankAccount> accounts) {
        return accounts.stream().collect(Collectors.groupingBy(BankAccount::getStatus, Collectors.counting()));
        // { ACTIVE: 2, FROZEN: 1, CLOSED: 1
    }

    public static Map<AccountStatus, Double> totalBalanceByStatus(List<BankAccount> accounts) {
        return accounts.stream().collect(Collectors.groupingBy(BankAccount::getStatus, Collectors.summingDouble(BankAccount::getBalance)));
        // Result: { ACTIVE: 5000.0, FROZEN: 800.0, CLOSED: 0.0 }
    }
    // Group by status AND average balance
    public static Map<AccountStatus, Double> avgBalanceByStatus(
            List<BankAccount> accounts) {
        return accounts.stream()
                .collect(Collectors.groupingBy(
                        BankAccount::getStatus,
                        Collectors.averagingDouble(BankAccount::getBalance)
                ));
    }

    // Group by status AND get full statistics
    public static Map<AccountStatus, DoubleSummaryStatistics> statsByStatus(
            List<BankAccount> accounts) {
        return accounts.stream()
                .collect(Collectors.groupingBy(
                        BankAccount::getStatus,
                        Collectors.summarizingDouble(BankAccount::getBalance)
                ));
        // Each group gets: count, sum, min, max, average
    }

    // Group by status AND collect names → Map<AccountStatus, List<String>>
    public static Map<AccountStatus, List<String>> namesByStatus(
            List<BankAccount> accounts) {
        return accounts.stream()
                .collect(Collectors.groupingBy(
                        BankAccount::getStatus,
                        Collectors.mapping(              // downstream: transform then collect
                                BankAccount::getOwnerName,
                                Collectors.toList()
                        )
                ));
        // Result: { ACTIVE: ["Alice", "Bob"], FROZEN: ["Carol"] }
    }

    // Group by status AND join names as string
    public static Map<AccountStatus, String> namesCsvByStatus(
            List<BankAccount> accounts) {
        return accounts.stream()
                .collect(Collectors.groupingBy(
                        BankAccount::getStatus,
                        Collectors.mapping(
                                BankAccount::getOwnerName,
                                Collectors.joining(", ")   // join into comma-separated
                        )
                ));
        // Result: { ACTIVE: "Alice, Bob", FROZEN: "Carol" }
    }

    // ================================================
    // TRANSACTION GROUPING — analyze transaction patterns
    // ================================================

    // Group all transactions by type → Map<TransactionType, List<Transaction>>
    public static Map<TransactionType, List<Transaction>> groupTxnsByType(
            List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getType));
    }

    // Sum amounts by type → Map<TransactionType, Double>
    public static Map<TransactionType, Double> totalByTxnType(
            List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getType,
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    // Count by type
    public static Map<TransactionType, Long> countByTxnType(
            List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getType,
                        Collectors.counting()
                ));
    }

    // Average amount by type
    public static Map<TransactionType, Double> avgByTxnType(
            List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getType,
                        Collectors.averagingDouble(Transaction::getAmount)
                ));
    }

    // ================================================
    // PARTITIONING — split into exactly 2 groups (true/false)
    // ================================================
    // partitioningBy always returns Map<Boolean, List<T>>

    // Split into rich and poor
    public static Map<Boolean, List<BankAccount>> partitionByWealth(
            List<BankAccount> accounts, double threshold) {
        return accounts.stream()
                .collect(Collectors.partitioningBy(
                        acc -> acc.getBalance() >= threshold
                ));
        // Result: { true: [rich accounts], false: [poor accounts] }
    }

    // Split into active and inactive
    public static Map<Boolean, List<BankAccount>> partitionByActive(
            List<BankAccount> accounts) {
        return accounts.stream()
                .collect(Collectors.partitioningBy(BankAccount::isActive));
    }

    // Partition with downstream counting
    public static Map<Boolean, Long> partitionCountByWealth(
            List<BankAccount> accounts, double threshold) {
        return accounts.stream()
                .collect(Collectors.partitioningBy(
                        acc -> acc.getBalance() >= threshold,
                        Collectors.counting()
                ));
        // Result: { true: 3, false: 7 }
    }

    // Partition transactions into credits and debits
    public static Map<Boolean, Double> partitionTxnTotals(
            List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.partitioningBy(
                        txn -> txn.getType().isCredit(),
                        Collectors.summingDouble(Transaction::getAmount)
                ));
        // Result: { true: 5000.0 (credits), false: 2000.0 (debits) }
    }

    // ================================================
    // toMap — create a lookup map from a stream
    // ================================================

    // Account number → Account object (like our accountMap)
    public static Map<String, BankAccount> buildAccountLookup(
            List<BankAccount> accounts) {
        return accounts.stream()
                .collect(Collectors.toMap(
                        BankAccount::getAccountNumber,  // key extractor
                        acc -> acc                       // value extractor (identity)
                ));
    }

    // Account number → Balance (simple key-value)
    public static Map<String, Double> buildBalanceLookup(
            List<BankAccount> accounts) {
        return accounts.stream()
                .collect(Collectors.toMap(
                        BankAccount::getAccountNumber,
                        BankAccount::getBalance
                ));
    }

    // Name → Account (careful: duplicate keys throw exception!)
    public static Map<String, BankAccount> buildNameLookup(
            List<BankAccount> accounts) {
        return accounts.stream()
                .collect(Collectors.toMap(
                        BankAccount::getOwnerName,
                        acc -> acc,
                        (existing, duplicate) -> existing  // merge: keep first if names clash
                ));
    }

    // ================================================
    // MULTI-LEVEL GROUPING — GROUP BY within GROUP BY
    // ================================================

    // Group by status, then within each status group by balance range
    public static Map<AccountStatus, Map<String, List<BankAccount>>> groupByStatusThenRange(
            List<BankAccount> accounts) {
        return accounts.stream()
                .collect(Collectors.groupingBy(
                        BankAccount::getStatus,
                        Collectors.groupingBy(acc -> {
                            if (acc.getBalance() >= 10000) return "High (≥$10K)";
                            if (acc.getBalance() >= 1000)  return "Medium ($1K-$10K)";
                            return "Low (<$1K)";
                        })
                ));
        // Result:
        // { ACTIVE: { "High": [Alice], "Medium": [Bob], "Low": [Carol] },
        //   FROZEN: { "Low": [Dave] } }
    }

    // ================================================
    // 🆕 PRACTICAL: Full advanced analytics dashboard
    // ================================================

    public static void printAdvancedDashboard(List<BankAccount> accounts) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║        ADVANCED STREAM ANALYTICS DASHBOARD       ║");
        System.out.println("╠══════════════════════════════════════════════════╣");

        if (accounts.isEmpty()) {
            System.out.println("║  No accounts to analyze.");
            System.out.println("╚══════════════════════════════════════════════════╝");
            return;
        }

        // --- Group by status ---
        System.out.println("║");
        System.out.println("║  📊 ACCOUNTS BY STATUS (groupingBy):");

        Map<AccountStatus, Long> statusCounts = countByStatus(accounts);
        Map<AccountStatus, Double> statusTotals = totalBalanceByStatus(accounts);

        statusCounts.forEach((status, count) -> {
            double total = statusTotals.getOrDefault(status, 0.0);
            System.out.printf("║    %s %-10s: %d accounts, $%,.2f total%n",
                    status.icon(), status, count, total);
        });

        // --- Partition rich/poor ---
        System.out.println("║");
        System.out.println("║  💰 WEALTH PARTITION (partitioningBy $1000):");

        Map<Boolean, List<BankAccount>> wealthPartition = partitionByWealth(accounts, 1000);
        List<BankAccount> rich = wealthPartition.get(true);
        List<BankAccount> poor = wealthPartition.get(false);

        System.out.println("║    Above $1,000: " + rich.size() + " accounts");
        rich.forEach(acc -> System.out.printf("║      • %s — $%,.2f%n",
                acc.getOwnerName(), acc.getBalance()));
        System.out.println("║    Below $1,000: " + poor.size() + " accounts");
        poor.forEach(acc -> System.out.printf("║      • %s — $%,.2f%n",
                acc.getOwnerName(), acc.getBalance()));

        // --- Names by status ---
        System.out.println("║");
        System.out.println("║  📋 NAMES BY STATUS (groupingBy + mapping + joining):");

        Map<AccountStatus, String> namesByStatus = namesCsvByStatus(accounts);
        namesByStatus.forEach((status, names) ->
                System.out.println("║    " + status.icon() + " " + status + ": " + names));

        // --- Transaction analysis (flatten all transactions) ---
        List<Transaction> allTxns = accounts.stream()
                .flatMap(acc -> acc.getTransactions().stream())
                .toList();

        if (!allTxns.isEmpty()) {
            System.out.println("║");
            System.out.println("║  🔄 TRANSACTIONS BY TYPE (groupingBy + summing):");

            Map<TransactionType, Double> txnTotals = totalByTxnType(allTxns);
            Map<TransactionType, Long> txnCounts = countByTxnType(allTxns);

            txnTotals.forEach((type, total) -> {
                long count = txnCounts.getOrDefault(type, 0L);
                System.out.printf("║    %-15s: %d transactions, %s total%n",
                        type.displayName(), count, type.formatAmount(total));
            });

            // --- Credit/debit split ---
            System.out.println("║");
            System.out.println("║  📈 CREDIT vs DEBIT (partitioningBy):");

            Map<Boolean, Double> creditDebit = partitionTxnTotals(allTxns);
            double credits = creditDebit.getOrDefault(true, 0.0);
            double debits = creditDebit.getOrDefault(false, 0.0);

            System.out.printf("║    Credits (in):  $%,.2f%n", credits);
            System.out.printf("║    Debits (out):  $%,.2f%n", debits);
            System.out.printf("║    Net flow:      $%,.2f%n", credits - debits);
        }

        // --- Balance distribution ---
        System.out.println("║");
        System.out.println("║  📊 BALANCE DISTRIBUTION (multi-level grouping):");

        Map<AccountStatus, Map<String, List<BankAccount>>> distribution =
                groupByStatusThenRange(accounts);

        distribution.forEach((status, ranges) -> {
            System.out.println("║    " + status.icon() + " " + status + ":");
            ranges.forEach((range, accs) ->
                    System.out.println("║      " + range + ": " + accs.size() + " account(s)"));
        });

        // --- Top spender (most total transaction value) ---
        System.out.println("║");
        System.out.println("║  🏆 TOP SPENDERS (flatMap + groupingBy):");

        // Group all transactions by account, sum amounts
        Map<String, Double> spendByAccount = allTxns.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getAccountNumber,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        // Sort by total spent (descending) and print top 3
        spendByAccount.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    // Look up account name
                    String name = accounts.stream()
                            .filter(a -> a.getAccountNumber().equals(entry.getKey()))
                            .map(BankAccount::getOwnerName)
                            .findFirst()
                            .orElse("Unknown");
                    System.out.printf("║    %s [%s]: $%,.2f total volume%n",
                            name, entry.getKey(), entry.getValue());
                });

        System.out.println("║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }
}
