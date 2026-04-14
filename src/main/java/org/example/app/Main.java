package app;

import org.example.model.BankAccount;
import org.example.service.AccountManager;
import service.AccountService;
import service.InputParser;
import java.util.Scanner;

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
        accountManager.createAccount("Abderrafii", 1000000.75);
        accountManager.createAccount("Bob Smith", 800.00);
        accountManager.createAccount("Carol Davis");
        System.out.println();

        // Do some initial transactions
        BankAccount Abderrafii = accountManager.getAccountByIndex(0);
        if (Abderrafii != null) {
            Abderrafii.deposit(500);
            Abderrafii.withdraw(150.50);
        }

        // 🆕 currentAccount is now selected from the dynamic list
        BankAccount currentAccount = accountManager.getAccountByIndex(0);
        boolean running = true;

        while (running) {
            System.out.println();

            if (currentAccount != null) {
                System.out.println("Current: " + currentAccount);
            } else {
                System.out.println("Current: [none selected]");
            }

            System.out.println("┌─────────────────────────────────────┐");
            System.out.println("│ 1.  Balance       2.  Deposit       │");
            System.out.println("│ 3.  Withdraw      4.  Account Report│");
            System.out.println("│ 5.  Mini Statement                  │");
            System.out.println("│ 6.  Bank Summary  7.  Search        │");
            System.out.println("│ 8.  Switch Acct   9.  New Account   │");
            System.out.println("│ 10. Close Account 11. Exit          │");
            System.out.println("└─────────────────────────────────────┘");

            int choice = InputParser.readInt(scanner, "Choose (1-11): ", 1, 11);

            // Guard — most options need a selected account
            if (currentAccount == null && choice <= 5) {
                System.out.println("⚠️  No account selected. Use option 8 to switch.");
                continue; // skip back to menu
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

                case 4 -> accountService.printAccountReport(currentAccount);

                case 5 -> currentAccount.printMiniStatement();

                case 6 -> accountService.printSummary();

                case 7 -> {
                    System.out.print("Search by name: ");
                    String search = scanner.nextLine();
                    accountService.searchByName(search);
                }

                // 🆕 Switch account — now works with ANY number of accounts
                case 8 -> {
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

                // 🆕 Create account at RUNTIME — dynamic list grows
                case 9 -> {
                    System.out.print("Owner name: ");
                    String name = scanner.nextLine();

                    System.out.print("Initial deposit (0 for none): $");
                    String depositStr = scanner.nextLine();
                    double deposit = 0;
                    try {
                        deposit = Double.parseDouble(depositStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount, using $0.");
                    }

                    BankAccount newAccount = accountManager.createAccount(name, deposit);
                    if (newAccount != null) {
                        currentAccount = newAccount; // auto-switch to new account
                        System.out.println("Switched to new account.");
                    }
                }

                // 🆕 Close account — removes from the list
                case 10 -> {
                    System.out.println("--- Close Account ---");
                    accountManager.printAccountList();
                    System.out.print("Enter account number (e.g., ACC-0001): ");
                    String accNum = scanner.nextLine().trim();

                    if (accountManager.closeAccount(accNum)) {
                        // If we closed the current account, switch away
                        if (currentAccount != null
                                && currentAccount.getAccountNumber().equals(accNum)) {
                            currentAccount = accountManager.hasAccounts()
                                    ? accountManager.getAccountByIndex(0)
                                    : null;
                            System.out.println("Switched to: "
                                    + (currentAccount != null ? currentAccount : "[none]"));
                        }
                    }
                }

                case 11 -> {
                    System.out.println("Goodbye! 👋");
                    running = false;
                }
            }
        }

        scanner.close();
        System.out.println("Thank you for banking with " + BankAccount.BANK_NAME + "!");
    }
}