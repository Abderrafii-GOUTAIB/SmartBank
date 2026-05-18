package org.example.service;

import org.example.model.*;
import org.example.model.exception.*;

import java.util.Comparator;
import java.util.List;

public class AccountService {

    // Each dependency has ONE job — injected via constructor
    private final AccountRepository repository;
    private final RiskService riskService;
    private final TransactionTracker tracker;

    public AccountService(AccountRepository repository,
                          RiskService riskService,
                          TransactionTracker tracker) {
        this.repository = repository;
        this.riskService = riskService;
        this.tracker = tracker;
    }

    // --- Account Creation ---

    public BankAccount openAccount(String ownerName, String email, double initialDeposit,
                                   AccountType accountType) {
        String normalizedEmail = email == null ? "" : email.toLowerCase().trim();

        if (repository.ownerNameExists(ownerName)) {
            throw new DuplicateNameException(ownerName);
        }

        if (repository.emailExists(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }



        BankAccount account = new BankAccount(ownerName, normalizedEmail, initialDeposit);
        repository.save(account);

        System.out.println("  ✅ Account opened: " + account);
        System.out.println("     Type: " + accountType.displayName()
                + " (" + String.format("%.1f%%", accountType.interestRate() * 100)
                + " interest, $" + String.format("%.2f", accountType.monthlyFee()) + "/month)");

        DataStore.appendLog("ACCOUNT_OPENED: " + account.getAccountNumber()
                + " " + ownerName + " " + accountType.displayName());

        return account;
    }

    // --- Deposit ---

    public TransactionResult deposit(BankAccount account, double amount) {
        try {
            account.deposit(amount);

            tracker.recordDeposit(account.getAccountNumber(), amount);
            tracker.recordCategory(TransactionType.DEPOSIT);
            riskService.checkTransaction(account, amount, "deposit");

            Transaction txn = account.getLastTransaction();
            System.out.println(ReceiptGenerator.generateTransactionReceipt(account, txn));

            return new TransactionResult.Success(
                    account.getAccountNumber(), amount,
                    account.getBalance(), TransactionType.DEPOSIT);

        } catch (AccountFrozenException e) {
            System.out.println("  ❌ " + e.toDisplayString());
            return new TransactionResult.Failure(
                    account.getAccountNumber(), amount,
                    e.getErrorCode(), e.getMessage());

        } catch (InvalidAmountException e) {
            System.out.println(" ❌ " + e.toDisplayString());
            return new TransactionResult.Failure(
                    account.getAccountNumber(), amount,
                    e.getErrorCode(), e.getMessage());
        }
    }

    // --- Withdrawal ---

    public TransactionResult withdraw(BankAccount account, double amount) {
        try {
            account.withdraw(amount);

            tracker.recordCategory(TransactionType.WITHDRAWAL);
            riskService.checkTransaction(account, amount, "withdrawal");

            Transaction txn = account.getLastTransaction();
            System.out.println(ReceiptGenerator.generateTransactionReceipt(account, txn));

            return new TransactionResult.Success(
                    account.getAccountNumber(), amount,
                    account.getBalance(), TransactionType.WITHDRAWAL);

        } catch (InsufficientFundsException e) {
            System.out.println("  ❌ " + e.toDisplayString());
            System.out.printf("     Requested: $%,.2f | Available: $%,.2f | Short: $%,.2f%n",
                    e.getRequested(), e.getAvailable(), e.getShortfall());
            return new TransactionResult.Failure(
                    account.getAccountNumber(), amount,
                    e.getErrorCode(), e.getMessage());

        } catch (AccountFrozenException | InvalidAmountException e) {
            System.out.println("  ❌ " + ((BankException) e).toDisplayString());
            return new TransactionResult.Failure(
                    account.getAccountNumber(), amount,
                    ((BankException) e).getErrorCode(), e.getMessage());
        }
    }

    // --- Transfer ---

    public TransferResult transfer(String fromAccNum, String toAccNum, double amount) {
        try {
            BankAccount from = repository.findByAccountNumber(fromAccNum);
            BankAccount to = repository.findByAccountNumber(toAccNum);

            if (fromAccNum.equals(toAccNum)) {
                System.out.println("  ❌ Cannot transfer to the same account.");
                return TransferResult.failure(fromAccNum, toAccNum, amount,
                        "Same account");
            }

            // Execute transfer (withdraw first, deposit second)
            boolean withdrawn = false;
            try {
                from.withdraw(amount);
                withdrawn = true;
                to.deposit(amount);
            } catch (Exception e) {
                // Rollback if deposit failed after withdrawal
                if (withdrawn) {
                    from.deposit(amount); // compensating transaction
                    System.out.println("  ⚠️  Transfer rolled back.");
                }
                throw e;
            }

            tracker.recordDeposit(to.getAccountNumber(), amount);
            tracker.recordCategory(TransactionType.TRANSFER_IN);
            tracker.recordCategory(TransactionType.TRANSFER_OUT);
            riskService.checkTransaction(from, amount, "transfer-out");

            TransferResult result = TransferResult.success(
                    fromAccNum, toAccNum, amount,
                    from.getBalance(), to.getBalance());

            System.out.println("  " + result.toDisplayString());

            DataStore.appendLog("TRANSFER: $" + amount + " "
                    + fromAccNum + " → " + toAccNum);

            return result;

        } catch (AccountNotFoundException e) {
            System.out.println("  ❌ " + e.toDisplayString());
            return TransferResult.failure(fromAccNum, toAccNum, amount, e.getMessage());

        } catch (InsufficientFundsException e) {
            System.out.println(" ❌ :" + e.toDisplayString());
            return TransferResult.failure(fromAccNum, toAccNum, amount, e.getMessage());

        } catch (AccountFrozenException e) {
            System.out.println(" ❌ " + e.toDisplayString());
            return TransferResult.failure(fromAccNum, toAccNum, amount, e.getMessage());
        }
    }

    // --- Close Account ---

    public boolean closeAccount(String accountNumber) {
        BankAccount account = repository.findByAccountNumber(accountNumber);

        if (account.getBalance() > 0) {
            System.out.println("  ⚠️  Account has $" + String.format("%,.2f", account.getBalance())
                    + " — withdraw funds first.");
            return false;
        }

        repository.remove(account);
        riskService.unflag(accountNumber);

        System.out.println("  🗑️  Account " + accountNumber + " closed.");
        DataStore.appendLog("ACCOUNT_CLOSED: " + accountNumber);
        return true;
    }

    // --- Reports ---

    public void printDashboard() {
        List<BankAccount> all = repository.getAll();

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║            SMARTBANK DASHBOARD               ║");
        System.out.println("╠══════════════════════════════════════════════╣");

        if (all.isEmpty()) {
            System.out.println("║  No accounts.");
            System.out.println("╚══════════════════════════════════════════════╝");
            return;
        }

        // Account stats using streams
        var stats = all.stream()
                .mapToDouble(BankAccount::getBalance)
                .summaryStatistics();

        long active = all.stream().filter(BankAccount::isActive).count();
        long frozen = all.stream().filter(a -> !a.isActive()).count();
        long flagged = all.stream()
                .filter(a -> riskService.isFlagged(a.getAccountNumber())).count();

        System.out.printf("║  Accounts: %d total | %d active | %d frozen | %d flagged%n",
                all.size(), active, frozen, flagged);
        System.out.printf("║  Total: $%,.2f | Avg: $%,.2f | Max: $%,.2f | Min: $%,.2f%n",
                stats.getSum(), stats.getAverage(), stats.getMax(), stats.getMin());

        // Top 3 by balance using sorted stream
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║  🏆 Top accounts:");

        all.stream()
                .sorted(Comparator.comparingDouble(BankAccount::getBalance).reversed())
                .limit(3)
                .forEach(acc -> {
                    String flag = riskService.isFlagged(acc.getAccountNumber()) ? " 🚩" : "";
                    System.out.printf("║    %s %s — $%,.2f%s%n",
                            acc.getStatus().icon(), acc.getOwnerName(),
                            acc.getBalance(), flag);
                });

        // All transactions across all accounts using flatMap
        long totalTxns = all.stream()
                .mapToInt(BankAccount::getTransactionCount)
                .sum();

        // Unique transaction types using distinct
        List<String> types = all.stream()
                .flatMap(acc -> acc.getTransactions().stream())
                .map(txn -> txn.getType().displayName())
                .distinct()
                .sorted()
                .toList();

        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║  Transactions: " + totalTxns + " total");
        System.out.println("║  Types seen: " + String.join(", ", types));
        System.out.println("╚══════════════════════════════════════════════╝");

        // Account types breakdown using streams + groupingBy
        var typeBreakdown = all.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        acc -> acc.getAccountType().displayName(),
                        java.util.stream.Collectors.counting()));

        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║  📊 By type:");
        typeBreakdown.forEach((type, count) ->
                System.out.println("║    " + type + ": " + count + " account(s)"));

        // Monthly interest preview
        double monthlyInterest = all.stream()
                .filter(BankAccount::isActive)
                .mapToDouble(BankAccount::calculateInterest)
                .sum();
        double monthlyFees = all.stream()
                .filter(BankAccount::isActive)
                .mapToDouble(BankAccount::getMonthlyFee)
                .sum();

        System.out.printf("║  💰 Monthly interest (projected): $%,.2f%n", monthlyInterest);
        System.out.printf("║  💸 Monthly fees (projected):     $%,.2f%n", monthlyFees);
    }

    public void printAccountReport(BankAccount account) {
        System.out.println(ReceiptGenerator.generateAccountReport(account));
    }

    public void searchAccounts(String searchTerm) {
        var results = repository.searchByName(searchTerm);

        System.out.println();
        if (results.isEmpty()) {
            System.out.println("  No accounts found matching \"" + searchTerm + "\"");
            return;
        }

        System.out.println("  Found " + results.size() + " account(s):");
        results.forEach(acc -> {
            String flag = riskService.isFlagged(acc.getAccountNumber()) ? " 🚩" : "";
            System.out.println("    " + acc + flag);
        });
    }

    // --- Data persistence ---

    public void saveAll() {
        try {
            List<BankAccount> all = repository.getAll();
            DataStore.saveAccounts(all);
            DataStore.saveTransactions(all);
            DataStore.saveFlagged(riskService.getFlaggedAccounts());
            System.out.println("  ✅ All data saved.");
        } catch (Exception e) {
            System.out.println("  ❌ Save failed: " + e.getMessage());
        }
    }

    public void loadAll() {
        try {
            var loaded = DataStore.loadAccounts();
            if (!loaded.isEmpty()) {
                repository.clear();
                repository.saveAll(loaded);
                riskService.loadFlags(DataStore.loadFlagged());
            }
        } catch (Exception e) {
            System.out.println("  ❌ Load failed: " + e.getMessage());
        }
    }

    // --- Accessors for Main to use ---

    public AccountRepository getRepository() {
        return repository;
    }

    public RiskService getRiskService() {
        return riskService;
    }

    public TransactionTracker getTracker() {
        return tracker;
    }

    public void applyMonthlyInterest() {
        System.out.println();
        System.out.println("--- Applying Monthly Interest ---");

        List<BankAccount> all = repository.getAll();
        double totalInterest = 0;

        for (BankAccount account : all) {
            if (!account.isActive()) {
                System.out.printf("  %s: skipped (%s)%n",
                        account.getAccountNumber(),
                        account.getStatus().description());
                continue;
            }

            // OCP in action: account.calculateInterest() calls
            // accountType.calculateInterest(balance)
            // Each type returns its own rate. We don't switch on type here.
            double interest = account.calculateInterest();

            if (interest > 0) {
                account.deposit(interest);
                tracker.recordCategory(TransactionType.INTEREST);
                totalInterest += interest;

                System.out.printf("  %s (%s): $%,.2f interest at %.1f%%%n",
                        account.getAccountNumber(),
                        account.getAccountType().displayName(),
                        interest,
                        account.getAccountType().interestRate() * 100);
            }
        }

        System.out.printf("  Total interest applied: $%,.2f%n", totalInterest);
        DataStore.appendLog("INTEREST_APPLIED: $" + String.format("%.2f", totalInterest));
    }

    public void applyMonthlyFees() {
        System.out.println();
        System.out.println("--- Applying Monthly Fees ---");

        List<BankAccount> all = repository.getAll();
        double totalFees = 0;

        for (BankAccount account : all) {
            if (!account.isActive()) continue;

            // OCP: account.getMonthlyFee() delegates to accountType.monthlyFee()
            double fee = account.getMonthlyFee();

            if (fee > 0) {
                try {
                    account.withdraw(fee);
                    tracker.recordCategory(TransactionType.FEE);
                    totalFees += fee;

                    System.out.printf("  %s (%s): $%.2f fee%n",
                            account.getAccountNumber(),
                            account.getAccountType().displayName(),
                            fee);
                } catch (Exception e) {
                    System.out.printf("  %s: fee failed — %s%n",
                            account.getAccountNumber(), e.getMessage());
                }
            } else {
                System.out.printf("  %s (%s): no fee ✅%n",
                        account.getAccountNumber(),
                        account.getAccountType().displayName());
            }
        }

        System.out.printf("  Total fees collected: $%,.2f%n", totalFees);
        DataStore.appendLog("FEES_APPLIED: $" + String.format("%.2f", totalFees));
    }
}