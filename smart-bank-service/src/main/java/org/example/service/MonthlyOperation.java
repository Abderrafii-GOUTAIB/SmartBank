package org.example.service;

import org.example.model.BankAccount;
import org.example.model.BankEvent;
import org.example.model.TransactionType;

import java.util.List;
import java.util.function.Consumer;

public abstract class MonthlyOperation {

    private final AccountRepository repository;
    private final Consumer<BankEvent> publisher;

    protected MonthlyOperation(AccountRepository repository, Consumer<BankEvent> publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public final void execute() {
        System.out.println();
        System.out.println("--- " + operationName() + "---");

        List<BankAccount> all = repository.getAll();
        double total = 0;
        for (BankAccount account : all) {
            if (!account.isActive()) {
                System.out.printf("  %s: skipped (%s)%n",
                        account.getAccountNumber(), account.getStatus().description());
                continue;
            }
            if (!shouldProcess(account)) continue;
            double amount = getAmount(account);
            if (amount > 0) {
                try {
                    apply(account, amount);
                    total += amount;
                    System.out.printf("  %s (%s): $%,.2f%n",
                            account.getAccountNumber(),
                            account.getAccountType().displayName(),
                            amount);
                    publisher.accept(new BankEvent.TransactionEvent(
                            account.getAccountNumber(),
                            getTransactionType(),
                            amount
                    ));
                } catch (Exception e) {
                    System.out.printf("  %s: failed — %s%n",
                            account.getAccountNumber(), e.getMessage());
                }
            } else {
                onZeroAmount(account);
            }
        }

        System.out.printf("  Total: $%,.2f%n", total);

        // fixed — publish system event
        publisher.accept(new BankEvent.SystemEvent(
                operationName().toUpperCase().replace(" ", "_")
                        + " total=$" + String.format("%.2f", total)));

    }

    protected abstract String operationName();

    protected abstract double getAmount(BankAccount account);

    protected abstract void apply(BankAccount account, double amount);

    protected abstract TransactionType getTransactionType();

    protected boolean shouldProcess(BankAccount account) {
        return true;
    }

    protected void onZeroAmount(BankAccount account) {
        System.out.printf("  %s (%s): no %s ✅%n",
                account.getAccountNumber(),
                account.getAccountType().displayName(),
                operationName().toLowerCase());
    }
}
