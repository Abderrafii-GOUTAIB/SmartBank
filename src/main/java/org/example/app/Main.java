package org.example.app;

import org.example.model.*;
import org.example.model.exception.*;
import org.example.service.*;

import java.util.ArrayList;
import java.util.Scanner;

import service.InputParser;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        StoragePort storage = new DataStore();
        AccountRepository repository = new AccountRepository();
        RiskService riskService = new RiskService(storage);
        TransactionTracker tracker = new TransactionTracker();
        AccountService bankService = new AccountService(repository, riskService, tracker, storage);
        String BANK_NAME = BankConfig.getInstance().getBankName();
        int AVAILABLE_ACCOUNT_TYPES = AccountTypeChoice.values().length;

        System.out.println("=== " + BANK_NAME + " ===");
        System.out.println();
        // Load saved data (if any)
        bankService.loadAll();

        // Seed accounts if empty (first run)
        if (repository.isEmpty()) {
            System.out.println("--- Welcome! Creating starter accounts ---");
            try {
                bankService.openAccount("Abderrafii", "abderrafii@goutaib.com", 50000, new AccountType.Savings());
                bankService.openAccount("Bob Smith", "bob@smith.com", 800.50, new AccountType.Checking());
                bankService.openAccount("Carol Davis", "carol@davis.com", 15000, new AccountType.Business());
            } catch (BankException e) {
                System.out.println("  ❌ " + e.toDisplayString());
            }
            System.out.println();
        }

        BankAccount currentAccount = repository.getByIndex(0);
        boolean running = true;

        while (running) {
            System.out.println();

            // Show current account with flag status
            if (currentAccount != null) {
                String flag = riskService.isFlagged(currentAccount.getAccountNumber()) ? " 🚩" : "";
                System.out.println("Current: " + currentAccount + flag);
            } else {
                System.out.println("Current: [none selected]");
            }

            System.out.println("╔═════════════════════════════════════╗");
            System.out.println("║         SMARTBANK MENU              ║");
            System.out.println("╠═════════════════════════════════════╣");
            System.out.println("║  ACCOUNTS                           ║");
            System.out.println("║   1. Open Account                   ║");
            System.out.println("║   2. View Account                   ║");
            System.out.println("║   3. Switch Account                 ║");
            System.out.println("║   4. Close Account                  ║");
            System.out.println("║   5. Freeze / Activate              ║");
            System.out.println("║                                     ║");
            System.out.println("║  TRANSACTIONS                       ║");
            System.out.println("║   6. Deposit                        ║");
            System.out.println("║   7. Withdraw                       ║");
            System.out.println("║   8. Transfer                       ║");
            System.out.println("║   9. Transaction History            ║");
            System.out.println("║                                     ║");
            System.out.println("║  REPORTS                            ║");
            System.out.println("║  10. Dashboard                      ║");
            System.out.println("║  11. Search Accounts                ║");
            System.out.println("║  12. Daily Activity                 ║");
            System.out.println("║                                     ║");
            System.out.println("║  RISK                               ║");
            System.out.println("║  13. Flag / Unflag Account          ║");
            System.out.println("║  14. View Flagged                   ║");
            System.out.println("║                                     ║");
            System.out.println("║  SYSTEM                             ║");
            System.out.println("║  15. Apply Interest                 ║");
            System.out.println("║  16. Apply Fees                     ║");
            System.out.println("║  17. Save                           ║");
            System.out.println("║  18. Exit                           ║");
            System.out.println("╚═════════════════════════════════════╝");

            int choice = InputParser.readInt(scanner, "Choose (1-18): ", 1, 18);

            // Guard: most options need a selected account
            if (currentAccount == null && choice >= 2 && choice <= 9) {
                System.out.println("  ⚠️  No account selected. Use option 3.");
                continue;
            }

            switch (choice) {

                // ═══════════════════════════════════
                // ACCOUNTS
                // ═══════════════════════════════════


                case 1 -> {
                    System.out.print("  Owner name: ");
                    String name = scanner.nextLine();
                    System.out.print("  Email: ");
                    String email = scanner.nextLine();


                    System.out.println("  Account type:");
                    AccountTypeFactory.getMenuLines().forEach(System.out::println);
                    int typeChoice = InputParser.readInt(scanner, "  Choose (1-" + AVAILABLE_ACCOUNT_TYPES + "): ", 1, AVAILABLE_ACCOUNT_TYPES);


                    AccountType accountType = AccountTypeFactory.create(typeChoice);

                    double deposit = InputParser.readPositiveDouble(scanner, "  Initial deposit: $");

                    try {
                        currentAccount = bankService.openAccount(name,email, deposit, accountType);
                    } catch (DuplicateNameException e) {
                        System.out.println("  ❌ " + e.getMessage());
                        System.out.println("  Please enter a different name.");
                    } catch (DuplicateEmailException e) {
                        System.out.println("  ❌ " + e.getMessage());
                        System.out.println("  Please enter a different email address");
                    } catch (BankException e) {
                        System.out.println("  ❌" + e.toDisplayString());
                    }
                }

                case 2 -> bankService.printAccountReport(currentAccount);

                case 3 -> {
                    if (repository.isEmpty()) {
                        System.out.println("  No accounts exist.");
                    } else {
                        System.out.println("  --- Select Account ---");
                        var all = repository.getAll();
                        for (int i = 0; i < all.size(); i++) {
                            String flag = riskService.isFlagged(all.get(i).getAccountNumber()) ? " 🚩" : "";
                            System.out.println("    " + (i + 1) + ". " + all.get(i) + flag);
                        }
                        int acc = InputParser.readInt(scanner, "  Choose (1-" + repository.count() + "): ", 1, repository.count());
                        currentAccount = repository.getByIndex(acc - 1);
                    }
                }

                case 4 -> {
                    System.out.print("  Account to close: ");
                    String accNum = scanner.nextLine().trim().toUpperCase();
                    try {
                        if (bankService.closeAccount(accNum)) {
                            if (currentAccount.getAccountNumber().equals(accNum)) {
                                currentAccount = repository.isEmpty() ? null : repository.getByIndex(0);
                            }
                        }
                    } catch (AccountNotFoundException e) {
                        System.out.println("  ❌ " + e.toDisplayString());
                    }
                }

                case 5 -> {
                    System.out.println("  Current status: " + currentAccount.getStatus().icon() + " " + currentAccount.getStatus().description());

                    var validTransitions = new ArrayList<AccountStatus>();
                    for (AccountStatus s : AccountStatus.values()) {
                        if (currentAccount.getStatus().canTransitionTo(s)) {
                            validTransitions.add(s);
                        }
                    }

                    if (validTransitions.isEmpty()) {
                        System.out.println("  No transitions available (terminal state).");
                    } else {
                        for (int i = 0; i < validTransitions.size(); i++) {
                            System.out.println("    " + (i + 1) + ". " + validTransitions.get(i).icon() + " " + validTransitions.get(i).description());
                        }
                        int statusChoice = InputParser.readInt(scanner, "  Choose: ", 1, validTransitions.size());
                        try {
                            currentAccount.changeStatus(validTransitions.get(statusChoice - 1));
                            storage.logAudit("STATUS_CHANGE: "                    // ← was DataStore.appendLog
                                    + currentAccount.getAccountNumber()
                                    + " → " + currentAccount.getStatus());
                        } catch (InvalidStatusTransitionException e) {
                            System.out.println("  ❌ " + e.toDisplayString());
                        }
                    }
                }

                // ═══════════════════════════════════
                // TRANSACTIONS
                // ═══════════════════════════════════

                case 6 -> {
                    double amount = InputParser.readPositiveDouble(scanner, "  Deposit: $");
                    bankService.deposit(currentAccount, amount);
                }

                case 7 -> {
                    double amount = InputParser.readPositiveDouble(scanner, "  Withdraw: $");
                    bankService.withdraw(currentAccount, amount);
                }

                case 8 -> {
                    System.out.println("  From: " + currentAccount);
                    System.out.print("  Destination account number: ");
                    String toAccNum = scanner.nextLine().trim().toUpperCase();
                    double amount = InputParser.readPositiveDouble(scanner, "  Amount: $");
                    bankService.transfer(currentAccount.getAccountNumber(), toAccNum, amount);
                }

                case 9 -> currentAccount.printMiniStatement();

                // ═══════════════════════════════════
                // REPORTS
                // ═══════════════════════════════════

                case 10 -> bankService.printDashboard();

                case 11 -> {
                    System.out.print("  Search by name: ");
                    bankService.searchAccounts(scanner.nextLine());
                }

                case 12 -> tracker.printDailyReport(repository);

                // ═══════════════════════════════════
                // RISK
                // ═══════════════════════════════════

                case 13 -> {
                    System.out.println("  1. Flag account");
                    System.out.println("  2. Unflag account");
                    int flagChoice = InputParser.readInt(scanner, "  Choose: ", 1, 2);
                    System.out.print("  Account number: ");
                    String accNum = scanner.nextLine().trim().toUpperCase();
                    if (flagChoice == 1) riskService.flag(accNum);
                    else riskService.unflag(accNum);
                }

                case 14 -> riskService.printFlaggedAccounts(repository);

                // ═══════════════════════════════════
                // SYSTEM
                // ═══════════════════════════════════

                case 15 -> bankService.applyMonthlyInterest();
                case 16 -> bankService.applyMonthlyFees();
                case 17 -> bankService.saveAll();

                case 18 -> {
                    System.out.print("  Save before exiting? (y/n): ");
                    if (scanner.nextLine().trim().toLowerCase().startsWith("y")) {
                        bankService.saveAll();
                    }
                    System.out.println("  Goodbye! 👋");
                    running = false;
                }
            }
        }

        scanner.close();
    }
}