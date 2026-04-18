package org.example.app;

import org.example.model.AccountStatus;
import org.example.model.BankAccount;
import org.example.model.TransactionType;
import org.example.service.AccountCleaner;
import org.example.service.AccountManager;
import org.example.service.AccountService;
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

        // --- Seed some accounts ---
        System.out.println("--- Creating Initial Accounts ---");
        accountManager.createAccount("Abderrafii", "goutaibabderrafii@gmail.com", 1000000.75);
        accountManager.createAccount("Bob Smith", "bob@smith.com", 800.50);
        accountManager.createAccount("Carol Davis", "carol@davis.com");
        System.out.println();

        System.out.println("--- Testing Duplicate Email ---");
        accountManager.createAccount("Abderrafii", "goutaibabderrafii@gmail.com", 100);
        // Even with a different name, the Set rejects it

        // 🆕 Test case-insensitive duplicate detection
        accountManager.createAccount("Bob Jr", "BOB@SMITH.COM", 100);
        // emails are normalized to lowercase, so this is also a duplicate


        // 🆕 currentAccount is now selected from the dynamic list

        System.out.println();

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

            System.out.println("┌────────────────────────────────────────────────┐");
            System.out.println("│  1. Balance              2. Deposit            │");
            System.out.println("│  3. Withdraw             4. Transfer           │");
            System.out.println("│  5. Account Report       6. Mini Statement     │");
            System.out.println("│  7. Bank Summary         8. Search             │");
            System.out.println("│  9. Find Account         10. Deposit Report    │");
            System.out.println("│ 11. Switch Acct          12. New Account       │");
            System.out.println("│ 13. Close Account        14. Flag Account      │");
            System.out.println("│ 15. Unflag Acct          16. Show Flagged      │");
            System.out.println("│ 17. Categories           18. Set Analytics     │");
            System.out.println("│ 19. Filter By transition 20. Cleanup Demo      │");
            System.out.println("│ 21. Freeze/Activate      22. Account Status    │");
            System.out.println("│ 23. Exit                                       │");
            System.out.println("└────────────────────────────────────────────────┘");


            int choice = InputParser.readInt(scanner, "Choose (1-22): ", 1, 22);

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
                    var found = accountManager.findByAccountNumber(
                            scanner.nextLine().trim().toUpperCase());
                    if (found != null) accountService.printAccountReport(found);
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

                    var newAcc = accountManager.createAccount(name, email, dep);
                    if (newAcc != null) {
                        currentAccount = newAcc;
                        System.out.println("Switched to new account.");
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
                    } catch (IllegalStateException e) {
                        System.out.println("❌ " + e.getMessage());
                    }
                }
                case 22 -> {
                    System.out.println(" ================= " + currentAccount.getAccountNumber() + "Account Status ===============");
                    AccountStatus status = currentAccount.getStatus();
                    System.out.println(currentAccount.getOwnerName() + " account status: " + status.icon() + " " + status.description());
                    System.out.println("Can perform transactions: " + (status.cannotTransact() ? "No" : "Yes"));
                    System.out.println("Is terminal state: " + (status.isTerminal() ? "Yes" : "No"));
                }
                case 23 -> {
                    System.out.println("Goodbye! 👋");
                    running = false;
                }
            }
        }

        scanner.close();
        System.out.println("Thank you for banking with " + BankAccount.BANK_NAME + "!");
    }
}