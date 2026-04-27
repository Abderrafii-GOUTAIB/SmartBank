package org.example.app;

import org.example.model.AccountStatus;
import org.example.model.BankAccount;
import org.example.model.Transaction;
import org.example.model.TransactionType;
import org.example.model.exception.AccountNotFoundException;
import org.example.model.exception.BankException;
import org.example.model.exception.DuplicateEmailException;
import org.example.model.exception.InvalidStatusTransitionException;
import org.example.service.*;
import service.InputParser;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 🆕 AccountManager holds the dynamic list of accounts
        AccountManager accountManager = new AccountManager();
        AccountService accountService = new AccountService(accountManager);

        System.out.println("=== " + BankAccount.BANK_NAME + " ===");
        System.out.println();

        // 🆕 FILE I/O: Try to load saved data first
        boolean loaded = accountManager.loadFromFiles();

        if (!loaded) {
            System.out.println("--- Creating Initial Accounts ---");
            try {
                accountManager.createAccount("Abderrafii", "goutaibabderrafii@gmail.com", 1000000.75);
                accountManager.createAccount("Bob Smith", "bob@smith.com", 800.50);
                accountManager.createAccount("Carol Davis", "carol@davis.com");
            } catch (BankException e) {
                System.out.println("❌ " + e.toDisplayString());
            }
            System.out.println();
            System.out.println("--- Testing Duplicate Email ---");
            try {
                accountManager.createAccount("Impostor", "goutaibabderrafii@gmail.com", 100);
            } catch (DuplicateEmailException e) {
                // 🆕 We can catch this SPECIFIC exception and access the email
                System.out.println("❌ " + e.toDisplayString());
                System.out.println("   Duplicate email: " + e.getEmail());
            }
            System.out.println();
        }

        BankAccount currentAccount = accountManager.getAccountByIndex(0);
        boolean running = true;

        while (running) {
            System.out.println();

            if (currentAccount != null) {
                String flag = accountManager.isFlagged(currentAccount.getAccountNumber()) ? " 🚩" : "";
                System.out.println("Current: " + currentAccount + flag);
            } else {
                System.out.println("Current: [none selected]");
            }

            System.out.println("┌──────────────────────────────────────────┐");
            System.out.println("│  1. Balance          2. Deposit          │");
            System.out.println("│  3. Withdraw         4. Transfer         │");
            System.out.println("│  5. Account Report   6. Mini Statement   │");
            System.out.println("│  7. Bank Summary     8. Search           │");
            System.out.println("│  9. Find Account     10. Deposit Report  │");
            System.out.println("│ 11. Switch Acct      12. New Account     │");
            System.out.println("│ 13. Close Account    14. Flag Account    │");
            System.out.println("│ 15. Unflag Acct      16. Show Flagged    │");
            System.out.println("│ 17. Categories       18. Set Analytics   │");
            System.out.println("│ 19. Filter Txns      20. Cleanup Demo    │");
            System.out.println("│ 21. Freeze/Activate  22. Account status  │");
            System.out.println("│ 23. Sort Accounts    24. Leaderboard     │");
            System.out.println("│ 25. Save Data        26. File info       │");
            System.out.println("│ 27. Today's Activity                     │");
            System.out.println("│ 28. Process Transactions                 │");
            System.out.println("│ 29. Method Ref Analytics                 │");
            System.out.println("│ 30. Method Ref Demo (4 types)            │");
            System.out.println("│ 31. Exit                                 │");
            System.out.println("└──────────────────────────────────────────┘");

            int choice = InputParser.readInt(scanner, "Choose (1-31): ", 1, 31);

            if (currentAccount == null && (choice >= 0)) {
                System.out.println("⚠️  No account selected. Use option 11.");
                continue;
            }


            switch (choice) {
                case 1 -> System.out.println("💰 Balance: $"
                        + String.format("%.2f", currentAccount.getBalance()));
                case 2 -> {
                    double amount = InputParser.readPositiveDouble(scanner, "Deposit amount: $");
                    accountService.processDeposit(currentAccount, amount);
                }
                case 3 -> {
                    double amount = InputParser.readPositiveDouble(scanner, "Withdrawal amount: $");
                    accountService.processWithdrawal(currentAccount, amount);
                }
                case 4 -> {
                    System.out.println("From: " + currentAccount);
                    accountManager.printAccountList();
                    System.out.print("Destination account: ");
                    String toAccNum = scanner.nextLine().trim().toUpperCase();
                    double amount = InputParser.readPositiveDouble(scanner, "Amount: $");
                    accountService.processTransfer(
                            currentAccount.getAccountNumber(), toAccNum, amount);
                }
                case 5 -> accountService.printAccountReport(currentAccount);
                case 6 -> currentAccount.printMiniStatement();
                case 7 -> accountService.printSummary();
                case 8 -> {
                    System.out.print("Search by name: ");
                    accountService.searchByName(scanner.nextLine());
                }
                case 9 -> {
                    System.out.print("Account number: ");
                    String accNum = scanner.nextLine().trim().toUpperCase();
                    try {
                        BankAccount found = accountManager.findByAccountNumber(accNum);
                        accountService.printAccountReport(found);
                    } catch (AccountNotFoundException e) {
                        System.out.println("❌ " + e.toDisplayString());
                    }
                }
                case 10 -> accountManager.printDailyDepositReport();
                case 11 -> {
                    if (!accountManager.hasAccounts()) {
                        System.out.println("No accounts exist.");
                    } else {
                        accountManager.printAccountList();
                        int acc = InputParser.readInt(scanner,
                                "Choose (1-" + accountManager.getAccountCount() + "): ",
                                1, accountManager.getAccountCount());
                        currentAccount = accountManager.getAccountByIndex(acc - 1);
                    }
                }
                case 12 -> {
                    System.out.print("Owner name: ");
                    String name = scanner.nextLine();
                    System.out.print("Email: ");
                    String email = scanner.nextLine();
                    System.out.print("Initial deposit: $");
                    String depStr = scanner.nextLine();
                    double dep = 0;
                    try {
                        dep = Double.parseDouble(depStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Using $0.");
                    }

                    try {
                        currentAccount = accountManager.createAccount(name, email, dep);
                        System.out.println("Switched to new account.");
                    } catch (DuplicateEmailException e) {
                        System.out.println("❌ " + e.toDisplayString());
                    } catch (BankException e) {
                        System.out.println("❌ " + e.toDisplayString());
                    }
                }
                case 13 -> {
                    accountManager.printAccountList();
                    System.out.print("Account to close: ");
                    String accNum = scanner.nextLine().trim().toUpperCase();
                    if (accountManager.closeAccount(accNum)) {
                        if (currentAccount.getAccountNumber().equals(accNum)) {
                            currentAccount = accountManager.hasAccounts()
                                    ? accountManager.getAccountByIndex(0) : null;
                        }
                    }
                }
                case 14 -> {
                    accountManager.printAccountList();
                    System.out.print("Account to flag: ");
                    accountManager.flagAccount(scanner.nextLine().trim().toUpperCase());
                }
                case 15 -> {
                    accountManager.printFlaggedAccounts();
                    System.out.print("Account to unflag: ");
                    accountManager.unflagAccount(scanner.nextLine().trim().toUpperCase());
                }
                case 16 -> accountManager.printFlaggedAccounts();
                case 17 -> accountManager.printCategories();
                case 18 -> {
                    System.out.println();
                    System.out.println("--- Set Analytics ---");
                    var richFlagged = accountManager.getRichFlaggedAccounts(1000000);
                    System.out.println("Rich AND flagged (≥$1.000.000):");
                    if (richFlagged.isEmpty()) System.out.println("  (none)");
                    else richFlagged.forEach(a -> System.out.println("  - " + a));

                    var richSafe = accountManager.getUnflaggedRichAccounts(500000);
                    System.out.println("Rich but NOT flagged:");
                    if (richSafe.isEmpty()) System.out.println("  (none)");
                    else richSafe.forEach(a -> System.out.println("  - " + a));
                }
                // 🆕 Filter transactions by type using custom iterator
                case 19 -> {
                    System.out.println("  Filter by transaction type:");
                    TransactionType[] types = TransactionType.values();
                    for (int i = 0; i < types.length; i++) {
                        System.out.println("  " + (i + 1) + ". " + types[i].displayName()
                                + (types[i].isCredit() ? " (+)" : " (-)"));
                    }
                    System.out.println("  " + (types.length + 1) + ". Show ALL");
                    int filter = InputParser.readInt(scanner, "Choose: ", 1, types.length + 1);
                    if (filter <= types.length) {
                        currentAccount.printTransactionsByType(types[filter - 1]);
                    } else {
                        currentAccount.printMiniStatement();
                    }
                }
                // 🆕 Cleanup demo — show the bug and three fixes
                case 20 -> {
                    System.out.println("=== ITERATOR DEMO ===");
                    System.out.println("Creating temporary test accounts...");

                    // Create a temporary list to demonstrate on
                    var tempList = new ArrayList<BankAccount>();
                    tempList.add(new BankAccount("Test-Rich", "rich@test.com", 5000));
                    tempList.add(new BankAccount("Test-Poor", "poor@test.com", 0));
                    tempList.add(new BankAccount("Test-Mid", "mid@test.com", 100));
                    tempList.add(new BankAccount("Test-Broke", "broke@test.com", 0));

                    System.out.println("Accounts: ");
                    tempList.forEach(a -> System.out.println("  " + a));

                    // Show the bug first
                    AccountCleaner.brokenCleanup(tempList);

                    System.out.println();
                    System.out.println("Now the CORRECT way (removeIf):");
                    AccountCleaner.cleanupWithRemoveIf(tempList, 1.0);

                    System.out.println();
                    System.out.println("Remaining: ");
                    tempList.forEach(a -> System.out.println("  " + a));
                }
                case 21 -> {
                    System.out.println("  Current status: "
                            + currentAccount.getStatus().icon() + " "
                            + currentAccount.getStatus().description());
                    System.out.println();
                    System.out.println("  Available transitions:");

                    // 🆕 Show only VALID transitions using the state machine
                    AccountStatus[] allStatuses = AccountStatus.values();
                    var validTransitions = new ArrayList<AccountStatus>();

                    for (AccountStatus s : allStatuses) {
                        if (currentAccount.getStatus().canTransitionTo(s)) {
                            validTransitions.add(s);
                            System.out.println("    " + validTransitions.size() + ". "
                                    + s.icon() + " " + s.description());
                        }
                    }

                    if (validTransitions.isEmpty()) {
                        System.out.println("    (none — this is a terminal state)");
                        break;
                    }

                    int statusChoice = InputParser.readInt(scanner,
                            "Choose transition (1-" + validTransitions.size() + "): ",
                            1, validTransitions.size());

                    try {
                        currentAccount.changeStatus(validTransitions.get(statusChoice - 1));
                    } catch (InvalidStatusTransitionException e) {
                        System.out.println("❌ " + e.toDisplayString());
                        System.out.println("   From: " + e.getFrom().icon() + " " + e.getFrom());
                        System.out.println("   To:   " + e.getTo().icon() + " " + e.getTo());
                    }
                }
                case 22 -> {
                    System.out.println(" ================= " + currentAccount.getAccountNumber() + "Account Status ===============");
                    AccountStatus status = currentAccount.getStatus();
                    System.out.println(currentAccount.getOwnerName() + " account status: " + status.icon() + " " + status.description());
                    System.out.println("Can perform transactions: " + (status.cannotTransact() ? "No" : "Yes"));
                    System.out.println("Is terminal state: " + (status.isTerminal() ? "Yes" : "No"));
                }
                // 🆕 Sort accounts — multiple Comparator strategies
                case 23 -> {
                    System.out.println();
                    System.out.println("--- Sort Accounts ---");
                    System.out.println("  1. By name (A → Z)");
                    System.out.println("  2. By name (Z → A)");
                    System.out.println("  3. By balance (lowest first)");
                    System.out.println("  4. By balance (highest first)");
                    System.out.println("  5. By most active (most transactions)");
                    System.out.println("  6. By account number");
                    System.out.println("  7. By status");
                    System.out.println("  8. By status, then balance");

                    int sortChoice = InputParser.readInt(scanner, "Sort by: ", 1, 8);

                    // 🆕 Select the right Comparator based on user choice
                    var comparator = switch (sortChoice) {
                        case 1 -> AccountComparators.BY_NAME;
                        case 2 -> AccountComparators.BY_NAME_DESC;
                        case 3 -> AccountComparators.BY_BALANCE;
                        case 4 -> AccountComparators.BY_BALANCE_DESC;
                        case 5 -> AccountComparators.BY_ACTIVITY;
                        case 7 -> AccountComparators.BY_STATUS;
                        case 8 -> AccountComparators.BY_STATUS_THEN_BALANCE;
                        default -> AccountComparators.BY_ACCOUNT_NUMBER;
                    };

                    accountManager.printAccountsSorted(comparator);
                }

                // 🆕 Leaderboard — top accounts by balance
                case 24 -> {
                    accountManager.printLeaderboard(5);

                    System.out.println();
                    // 🆕 Show min/max using Collections.min/max with Comparator
                    var richest = accountManager.getRichestAccount();
                    var poorest = accountManager.getPoorestAccount();
                    var busiest = accountManager.getMostActiveAccount();

                    if (richest != null) {
                        System.out.println("  💰 Richest:     " + richest.getOwnerName()
                                + " ($" + String.format("%,.2f", richest.getBalance()) + ")");
                        System.out.println("  📉 Lowest:      " + poorest.getOwnerName()
                                + " ($" + String.format("%,.2f", poorest.getBalance()) + ")");
                        System.out.println("  ⚡ Most active:  " + busiest.getOwnerName()
                                + " (" + busiest.getTransactionCount() + " transactions)");
                    }
                }
                case 25 -> accountManager.saveToFiles();
                // 🆕 Show file info
                case 26 -> DataStore.printFileInfo();
                // 🆕 Exit — auto-save before quitting
                // 🆕 Today's activity — time-based queries
                case 27 -> {
                    System.out.println();
                    System.out.println("--- Today's Activity for " + currentAccount.getOwnerName() + " ---");
                    System.out.println("Account created: " + currentAccount.getFormattedCreatedAt());
                    System.out.println("Account age:     " + currentAccount.getAccountAge());
                    System.out.println();

                    // Today's transactions
                    var todayTxns = currentAccount.getTodaysTransactions();
                    System.out.println("Transactions today: " + todayTxns.size());

                    if (!todayTxns.isEmpty()) {
                        for (Transaction txn : todayTxns) {
                            System.out.println("  " + txn.getShortDateTime()
                                    + " | " + txn.getType().displayName()
                                    + " | " + txn.getType().formatAmount(txn.getAmount())
                                    + " | " + txn.getTimeAgo());
                        }

                        System.out.println();
                        System.out.printf("  Today's deposits:    $%.2f%n",
                                currentAccount.getTodaysTotalByType(TransactionType.DEPOSIT));
                        System.out.printf("  Today's withdrawals: $%.2f%n",
                                currentAccount.getTodaysTotalByType(TransactionType.WITHDRAWAL));
                    }

                    // Recent activity
                    System.out.println();
                    var recentTxns = currentAccount.getRecentTransactions(1);
                    System.out.println("In the last hour: " + recentTxns.size() + " transaction(s)");
                }
                case 28 -> {

                    if (currentAccount.getTransactionCount() == 0) {
                        System.out.println("⚠️  No transactions yet. Make some deposits/withdrawals first.");
                        break;
                    }


                    System.out.println();
                    System.out.println("--- Transaction Processing (Lambdas) ---");
                    System.out.println("  1. Filter: deposits only");
                    System.out.println("  2. Filter: large transactions (> $500)");
                    System.out.println("  3. Filter: large deposits (combined)");
                    System.out.println("  4. Transform: receipt format");
                    System.out.println("  5. Transform: CSV export");
                    System.out.println("  6. Process: detailed (multi-method handler)");
                    System.out.println("  7. Process: summary (handler with state)");
                    System.out.println("  8. Sort: by amount (highest first)");
                    System.out.println("  9. Custom filter (enter min amount)");
                    System.out.println(" 10. Lambda analytics");

                    int procChoice = InputParser.readInt(scanner, "Choose (1-10): ", 1, 10);

                    var txns = currentAccount.getTransactions();

                    switch (procChoice) {

                        // Pre-defined anonymous class filter
                        // 🆕 Predefined lambda filter
                        case 1 -> {
                            var filtered = TransactionProcessor.filterTransactions(
                                    txns, TransactionProcessor.DEPOSITS_ONLY);
                            System.out.println("Deposits (" + filtered.size() + "):");
                            filtered.forEach(t -> System.out.println("  " + t));
                            //              ↑ THIS is also a lambda! forEach takes Consumer<T>
                        }

                        // 🆕 Factory method returns a lambda
                        case 2 -> {
                            var filtered = TransactionProcessor.filterTransactions(
                                    txns, TransactionProcessor.aboveAmount(500));
                            System.out.println("Large transactions (" + filtered.size() + "):");
                            filtered.forEach(t -> System.out.println("  " + t));
                        }

                        // 🆕 Combined lambdas
                        case 3 -> {
                            var combined = TransactionProcessor.and(
                                    TransactionProcessor.DEPOSITS_ONLY,
                                    TransactionProcessor.aboveAmount(500)
                            );
                            var filtered = TransactionProcessor.filterTransactions(txns, combined);
                            System.out.println("Large deposits (" + filtered.size() + "):");
                            filtered.forEach(t -> System.out.println("  " + t));
                        }

                        case 4 -> {
                            var lines = TransactionProcessor.transformTransactions(
                                    txns, TransactionProcessor.RECEIPT_FORMAT);
                            System.out.println("Receipt format:");
                            lines.forEach(System.out::println);
                            // ↑ System.out::println is a METHOD REFERENCE (Step 23!)
                        }

                        case 5 -> {
                            var lines = TransactionProcessor.transformTransactions(
                                    txns, TransactionProcessor.CSV_FORMAT);
                            System.out.println("id,type,amount,balance,datetime");
                            lines.forEach(System.out::println);
                        }

                        // Multi-method handlers — still anonymous classes (can't be lambdas)
                        case 6 -> TransactionProcessor.processAll(
                                currentAccount, TransactionProcessor.CONSOLE_HANDLER);

                        case 7 -> TransactionProcessor.processAll(
                                currentAccount, TransactionProcessor.SUMMARY_HANDLER);

                        case 8 -> {
                            var sorted = TransactionProcessor.sortByAmountDescending(txns);
                            System.out.println("Sorted by amount (highest first):");
                            sorted.forEach(t -> System.out.println("  " + t));
                        }

                        // 🆕 INLINE LAMBDA — created right here, no class at all
                        case 9 -> {
                            double min = InputParser.readPositiveDouble(scanner, "Minimum amount: $");

                            // BEFORE (Step 21 — anonymous class):
                            // TransactionProcessor.TransactionFilter customFilter =
                            //     new TransactionProcessor.TransactionFilter() {
                            //         @Override
                            //         public boolean accept(Transaction transaction) {
                            //             return transaction.getAmount() >= min;
                            //         }
                            //     };

                            // AFTER (lambda — one line):
                            var filtered = TransactionProcessor.filterTransactions(
                                    txns, t -> t.getAmount() >= min);
                            //        ↑ the entire filter is this tiny lambda

                            System.out.println("Transactions ≥ $" + String.format("%.2f", min)
                                    + " (" + filtered.size() + "):");
                            filtered.forEach(t -> System.out.println("  " + t));
                        }

                        // 🆕 Lambda analytics — higher-order functions
                        case 10 -> {
                            System.out.println("--- Lambda Analytics ---");

                            // forEach with lambda — custom action for each transaction
                            System.out.println("All transactions:");
                            TransactionProcessor.forEach(txns,
                                    t -> System.out.println("  " + t.getType().formatAmount(t.getAmount())
                                            + " " + t.getTimeAgo()));

                            // sumBy with lambda — extract a value and sum it
                            double totalDeposits = TransactionProcessor.sumBy(txns,
                                    t -> t.getType().isCredit() ? t.getAmount() : 0);
                            double totalWithdrawals = TransactionProcessor.sumBy(txns,
                                    t -> t.getType().isDebit() ? t.getAmount() : 0);

                            System.out.printf("%n  Total credits:  $%.2f%n", totalDeposits);
                            System.out.printf("  Total debits:   $%.2f%n", totalWithdrawals);
                            System.out.printf("  Net:            $%.2f%n", totalDeposits - totalWithdrawals);

                            // count with lambda
                            int largeCount = TransactionProcessor.count(txns,
                                    t -> t.getAmount() > 1000);
                            System.out.println("  Large transactions (>$1000): " + largeCount);

                            // findFirst with lambda
                            Transaction biggest = TransactionProcessor.findFirst(
                                    TransactionProcessor.sortByAmountDescending(txns),
                                    t -> true);  // first in descending = biggest
                            if (biggest != null) {
                                System.out.println("  Biggest: " + biggest.getType().formatAmount(biggest.getAmount()));
                            }
                        }
                    }
                }
                case 29 -> {
                    if (currentAccount.getTransactionCount() == 0) {
                        System.out.println("⚠️  No transactions yet.");
                        break;
                    }
                    TransactionAnalytics.runFullAnalytics(currentAccount);
                }
                case 30 -> {
                    if (currentAccount.getTransactionCount() == 0) {
                        System.out.println("⚠️  No transactions yet.");
                        break;
                    }

                    var txns = currentAccount.getTransactions();

                    System.out.println();
                    System.out.println("=== Method Reference Types Demo ===");
                    System.out.println("  1. Type 1: Static method ref");
                    System.out.println("  2. Type 2: Instance on specific object");
                    System.out.println("  3. Type 3: Instance on arbitrary object");
                    System.out.println("  4. Type 4: Constructor ref");
                    System.out.println("  5. All four");

                    int demoChoice = InputParser.readInt(scanner, "Choose (1-5): ", 1, 5);

                    switch (demoChoice) {
                        case 1 -> TransactionAnalytics.demonstrateStaticMethodRef(txns);
                        case 2 -> TransactionAnalytics.demonstrateInstanceOnObject(txns, currentAccount);
                        case 3 -> TransactionAnalytics.demonstrateInstanceOnArbitrary(txns);
                        case 4 -> TransactionAnalytics.demonstrateConstructorRef(txns);
                        case 5 -> {
                            TransactionAnalytics.demonstrateStaticMethodRef(txns);
                            System.out.println();
                            TransactionAnalytics.demonstrateInstanceOnObject(txns, currentAccount);
                            System.out.println();
                            TransactionAnalytics.demonstrateInstanceOnArbitrary(txns);
                            System.out.println();
                            TransactionAnalytics.demonstrateConstructorRef(txns);
                        }
                    }
                }
                case 31 -> {
                    System.out.println();
                    System.out.print("Save before exiting? (y/n): ");
                    String answer = scanner.nextLine().trim().toLowerCase();
                    if (answer.startsWith("y")) {
                        accountManager.saveToFiles();
                    }
                    System.out.println("Goodbye! 👋");
                    running = false;
                }
            }
        }

        scanner.close();
        System.out.println("Thank you for banking with " + BankAccount.BANK_NAME + "!");
    }
}