package app;

import org.example.model.BankAccount;
import org.example.service.AccountService;
import service.InputParser;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AccountService accountService = new AccountService();

        System.out.println("=== " + BankAccount.BANK_NAME + " ===");
        System.out.println();

        // --- Create accounts ---
        System.out.println("--- Creating Accounts ---");
        BankAccount alice = accountService.createAccount("Alice Johnson", 2500.75);
        BankAccount bob   = accountService.createAccount("Bob Smith", 800.00);
        BankAccount carol = accountService.createAccount("Carol Davis");
        System.out.println();

        // Do a few transactions so reports have data
        alice.deposit(500);
        alice.withdraw(150.50);
        bob.deposit(1200);

        accountService.printSummary(alice, bob, carol);

        // --- Menu ---
        BankAccount currentAccount = alice;
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("Operating on: " + currentAccount);
            System.out.println("┌───────────────────────────────────┐");
            System.out.println("│ 1. Balance     2. Deposit         │");
            System.out.println("│ 3. Withdraw    4. Account Report  │");
            System.out.println("│ 5. Statement   6. Bank Summary    │");
            System.out.println("│ 7. Search      8. Switch Account  │");
            System.out.println("│ 9. Exit                           │");
            System.out.println("└───────────────────────────────────┘");

            int choice = InputParser.readInt(scanner, "Choose (1-9): ", 1, 9);

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

                // 🆕 Full account report with transaction table
                case 4 -> accountService.printAccountReport(currentAccount);

                case 5 -> currentAccount.printMiniStatement();

                case 6 -> accountService.printSummary(alice, bob, carol);

                // 🆕 Search by name
                case 7 -> {
                    System.out.print("Search by name: ");
                    String search = scanner.nextLine();
                    accountService.searchByName(search, alice, bob, carol);
                }

                case 8 -> {
                    System.out.println("  1. " + alice);
                    System.out.println("  2. " + bob);
                    System.out.println("  3. " + carol);
                    int acc = InputParser.readInt(scanner, "Choose account (1-3): ", 1, 3);
                    currentAccount = switch (acc) {
                        case 1 -> alice;
                        case 2 -> bob;
                        case 3 -> carol;
                        default -> currentAccount;
                    };
                }

                case 9 -> {
                    System.out.println("Goodbye! 👋");
                    running = false;
                }
            }
        }

        scanner.close();
        System.out.println("Thank you for banking with " + BankAccount.BANK_NAME + "!");
    }
}