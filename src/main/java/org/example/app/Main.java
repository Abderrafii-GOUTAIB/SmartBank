package org.example.app;
import org.example.model.BankAccount;
import org.example.service.AccountService;


import java.util.Scanner;
import service.InputParser;


public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AccountService accountService = new AccountService();

        System.out.println("=========" + BankAccount.BANK_NAME+ "==========");
        System.out.println("Total accounts" + BankAccount.getTotalAccounts()+ "!");

        // 🆕 Demonstrate the wrapper comparison trap
        InputParser.demonstrateWrapperTrap();
        System.out.println();

        // --- Create accounts through the service ---
        System.out.println("--- Creating Accounts ---");

        BankAccount account = accountService.createAccount("Alice", 1000.0);
        BankAccount account2 = accountService.createAccount("Bob", 500.0);
        BankAccount account3 = accountService.createAccount("Charlie");

        BankAccount currentAccount = account;
        boolean running = true;


        while (running) {
            System.out.println();
            System.out.println("╔═══════════════════════════╗");
            System.out.println("║      SMARTBANK MENU       ║");
            System.out.println("╠═══════════════════════════╣");
            System.out.println("║  1. Balance               ║");
            System.out.println("║  2. Deposit               ║");
            System.out.println("║  3. Withdraw              ║");
            System.out.println("║  4. Account Info          ║");
            System.out.println("║  5. Statement             ║");
            System.out.println("║  6. Summary               ║");
            System.out.println("║  7. Switch account        ║");
            System.out.println("║  8. Exit                  ║");
            System.out.println("╚═══════════════════════════╝");
            System.out.print("Choose (1-8): ");

            try {
                int choice = InputParser.readInt(scanner, "Choose (1-8): ", 1, 8);
                switch (choice) {
                    case 1 -> System.out.println("💰 Balance: $"
                            + String.format("%.2f", currentAccount.getBalance()));

                    case 2 -> {
                        // 🆕 readPositiveDouble — keeps asking until valid
                        double amount = InputParser.readPositiveDouble(scanner, "Deposit amount: $");
                        accountService.processDeposit(currentAccount, amount);
                    }

                    case 3 -> {
                        double amount = InputParser.readPositiveDouble(scanner, "Withdrawal amount: $");
                        accountService.processWithdrawal(currentAccount, amount);
                    }

                    case 4 -> currentAccount.displayInfo();

                    case 5 -> currentAccount.printMiniStatement();

                    case 6 -> accountService.printSummary(account, account2, account3);

                    case 7 -> {
                        System.out.println("  1. " + account);
                        System.out.println("  2. " + account2);
                        System.out.println("  3. " + account3);
                        int acc = InputParser.readInt(scanner, "Choose account (1-3): ", 1, 3);
                        currentAccount = switch (acc) {
                            case 1 -> account;
                            case 2 -> account2;
                            case 3 -> account3;
                            default -> currentAccount;
                        };
                    }
                    case 8 -> {
                        System.out.println("Goodbye! 👋");
                        running = false;
                    }
                    default -> System.out.println("❌ Invalid option.");
                }


            }
            catch (java.util.InputMismatchException e) {
                System.out.println("❌ Please enter a valid number!");
                scanner.nextLine(); // 🆕 CRITICAL: clear the bad input from scanner
                // Without this line, the scanner still has "abc" in it
                // and the next nextInt() would immediately fail again
                // creating an infinite error loop!
            } catch (Exception e) {
                System.out.println("⚠️  Unexpected error: " + e.getMessage());
                scanner.nextLine(); // clear scanner just in case

            }

        }

        scanner.close();
        System.out.println();
        System.out.println("Thank you for banking with " + BankAccount.BANK_NAME + "!");

    }
}