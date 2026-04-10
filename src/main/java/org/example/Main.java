package org.example;

import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        BankAccount account = new BankAccount("ACC-001", "Alice Johnson", 2500.75);

        System.out.println("=== SmartBank ===");
        System.out.println("Welcome, " + account.getOwnerName() + "!");

        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("╔═══════════════════════════╗");
            System.out.println("║      SMARTBANK MENU       ║");
            System.out.println("╠═══════════════════════════╣");
            System.out.println("║  1. Check Balance         ║");
            System.out.println("║  2. Deposit               ║");
            System.out.println("║  3. Withdraw              ║");
            System.out.println("║  4. Account Info          ║");
            System.out.println("║  5. Mini Statement        ║");
            System.out.println("║  6. Exit                  ║");
            System.out.println("╚═══════════════════════════╝");
            System.out.print("Choose (1-6): ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> System.out.println("💰 Balance: $"
                        + String.format("%.2f", account.getBalance()));
                case 2 -> {
                    System.out.print("Deposit amount: $");
                    account.deposit(scanner.nextDouble());
                }
                case 3 -> {
                    System.out.print("Withdrawal amount: $");
                    account.withdraw(scanner.nextDouble());
                }
                case 4 -> account.displayInfo();
                case 5 -> account.printMiniStatement();
                case 6 -> {
                    System.out.println("Goodbye! 👋");
                    running = false;
                }
                default -> System.out.println("❌ Invalid option.");
            }
        }

        scanner.close();
    }
}