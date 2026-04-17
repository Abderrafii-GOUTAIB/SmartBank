package app;

import org.example.model.BankAccount;
import org.example.service.AccountCleaner;
import org.example.service.AccountManager;
import service.AccountService;
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
        accountManager.createAccount("Abderrafii", "goutaibabderrafii@gmail.com",1000000.75);
        accountManager.createAccount("Bob Smith", "bob@smith.com",800.50);
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



            System.out.println("┌───────────────────────────────────────┐");
            System.out.println("│  1. Balance        2. Deposit         │");
            System.out.println("│  3. Withdraw       4. Transfer        │");
            System.out.println("│  5. Account Report 6. Mini Statement  │");
            System.out.println("│  7. Bank Summary   8. Search          │");
            System.out.println("│  9. Find Account  10. Deposit Report  │");
            System.out.println("│ 11. Switch Acct   12. New Account     │");
            System.out.println("│ 13. Close Account 14. Flag Account    │");
            System.out.println("│ 15. Unflag Acct   16. Show Flagged    │");
            System.out.println("│ 17. Categories    18. Set Analytics   │");
            System.out.println("│ 19. Filter Txns   20. Cleanup Demo    │");
            System.out.println("│ 21. Exit                              │");
            System.out.println("└───────────────────────────────────────┘");

            int choice = InputParser.readInt(scanner, "Choose (1-21): ", 1, 21);

            if (currentAccount == null && (choice <= 6  || choice == 19 )) {
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
                    try { dep = Double.parseDouble(depStr); }
                    catch (NumberFormatException e) { System.out.println("Using $0."); }

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
                        if (currentAccount != null
                                && currentAccount.getAccountNumber().equals(accNum)) {
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
                    var richFlagged = accountManager.getRichFlaggedAccounts(500);
                    System.out.println("Rich AND flagged (≥$500):");
                    if (richFlagged.isEmpty()) System.out.println("  (none)");
                    else richFlagged.forEach(a -> System.out.println("  - " + a));

                    var richSafe = accountManager.getUnflaggedRichAccounts(500);
                    System.out.println("Rich but NOT flagged:");
                    if (richSafe.isEmpty()) System.out.println("  (none)");
                    else richSafe.forEach(a -> System.out.println("  - " + a));
                }

                // 🆕 Filter transactions by type using custom iterator
                case 19 -> {
                    System.out.println("  1. Show only DEPOSITS");
                    System.out.println("  2. Show only WITHDRAWALS");
                    System.out.println("  3. Show ALL");
                    int filter = InputParser.readInt(scanner, "Choose: ", 1, 3);
                    switch (filter) {
                        case 1 -> currentAccount.printTransactionsByType("DEPOSIT");
                        case 2 -> currentAccount.printTransactionsByType("WITHDRAWAL");
                        case 3 -> currentAccount.printMiniStatement();
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
                    System.out.println("Goodbye! 👋");
                    running = false;
                }
            }
        }

        scanner.close();
        System.out.println("Thank you for banking with " + BankAccount.BANK_NAME + "!");
    }
}