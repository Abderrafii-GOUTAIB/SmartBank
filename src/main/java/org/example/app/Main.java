package app;

import org.example.model.BankAccount;
import org.example.service.AccountManager;
import service.AccountService;
import service.InputParser;
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
            System.out.println("│ 19. Exit                              │");
            System.out.println("└───────────────────────────────────────┘");

            int choice = InputParser.readInt(scanner, "Choose (1-19): ", 1, 19);

            if (currentAccount == null && choice <= 6) {
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
                    System.out.println("--- Transfer Funds ---");
                    System.out.println("From: " + currentAccount);
                    accountManager.printAccountList(currentAccount);
                    System.out.print("Destination account (e.g., ACC-0002): ");
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
                    String search = scanner.nextLine();
                    accountService.searchByName(search);
                }

                case 9 -> {
                    System.out.print("Account number: ");
                    String accNum = scanner.nextLine().trim().toUpperCase();
                    BankAccount found = accountManager.findByAccountNumber(accNum);
                    if (found != null) accountService.printAccountReport(found);
                }

                case 10 -> accountManager.printDailyDepositReport();

                case 11 -> {
                    if (!accountManager.hasAccounts()) {
                        System.out.println("No accounts exist yet.");
                    } else {
                        System.out.println("--- Select Account ---");
                        accountManager.printAccountList();
                        int acc = InputParser.readInt(scanner,
                                "Choose (1-" + accountManager.getAccountCount() + "): ",
                                1, accountManager.getAccountCount());
                        currentAccount = accountManager.getAccountByIndex(acc - 1);
                    }
                }

                // 🆕 Create account with email
                case 12 -> {
                    System.out.print("Owner name: ");
                    String name = scanner.nextLine();
                    System.out.print("Email: ");
                    String email = scanner.nextLine();
                    System.out.print("Initial deposit (0 for none): $");
                    String depositStr = scanner.nextLine();
                    double deposit = 0;
                    try {
                        deposit = Double.parseDouble(depositStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount, using $0.");
                    }

                    BankAccount newAccount = accountManager.createAccount(name, email, deposit);
                    if (newAccount != null) {
                        currentAccount = newAccount;
                        System.out.println("Switched to new account.");
                    }
                }

                case 13 -> {
                    System.out.println("--- Close Account ---");
                    accountManager.printAccountList();
                    System.out.print("Account number: ");
                    String accNum = scanner.nextLine().trim().toUpperCase();
                    if (accountManager.closeAccount(accNum)) {
                        if (currentAccount != null
                                && currentAccount.getAccountNumber().equals(accNum)) {
                            currentAccount = accountManager.hasAccounts()
                                    ? accountManager.getAccountByIndex(0) : null;
                        }
                    }
                }

                // 🆕 Flag / Unflag / Show flagged
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

                // 🆕 Show unique transaction categories (TreeSet sorted)
                case 17 -> accountManager.printCategories();

                // 🆕 Set operations demo — intersection and difference
                case 18 -> {
                    System.out.println();
                    System.out.println("--- Set Analytics ---");

                    Set<String> richFlagged = accountManager.getRichFlaggedAccounts(1000);
                    System.out.println("Rich AND flagged (balance ≥ $1000):");
                    if (richFlagged.isEmpty()) {
                        System.out.println("  (none)");
                    } else {
                        richFlagged.forEach(a -> System.out.println("  - " + a));
                    }

                    Set<String> richSafe = accountManager.getRichFlaggedAccounts(1000);
                    System.out.println("Rich but NOT flagged:");
                    if (richSafe.isEmpty()) {
                        System.out.println("  (none)");
                    } else {
                        richSafe.forEach(a -> System.out.println("  - " + a));
                    }
                }

                case 19 -> {
                    System.out.println("Goodbye! 👋");
                    running = false;
                }
            }
        }

        scanner.close();
        System.out.println("Thank you for banking with " + BankAccount.BANK_NAME + "!");
    }
}