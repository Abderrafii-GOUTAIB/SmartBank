package org.example.service;

import org.example.model.BankAccount;
import org.example.model.Transaction;
import org.example.model.TransactionType;

import java.util.*;
import java.util.stream.Collectors;

public class TransactionTracker {

    private final Map<String, Double> dailyDeposits;
    private final Set<String> transactionCategories;

    public TransactionTracker() {
        this.dailyDeposits = new HashMap<>();
        this.transactionCategories = new TreeSet<>();
    }

    // --- Recording ---

    public void recordDeposit(String accountNumber, double amount) {
        dailyDeposits.merge(accountNumber, amount, Double::sum);
    }

    public void recordCategory(TransactionType type) {
        transactionCategories.add(type.displayName());
    }

    // --- Queries ---

    public double getDailyTotal(String accountNumber) {
        return dailyDeposits.getOrDefault(accountNumber, 0.0);
    }

    public double getTotalDailyDeposits() {
        return dailyDeposits.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    // --- Reports ---

    public void printDailyReport(AccountRepository repo) {
        System.out.println();
        System.out.println("--- Daily Activity Report ---");

        if (dailyDeposits.isEmpty()) {
            System.out.println("  No activity today.");
            return;
        }

        dailyDeposits.forEach((accNum, total) -> {
            if (total > 0) {
                String name = repo.findByAccountNumberSafe(accNum)
                        .map(BankAccount::getOwnerName)
                        .orElse("Unknown");
                System.out.printf("  %s (%s): $%,.2f%n", accNum, name, total);
            }
        });
        System.out.printf("  TOTAL: $%,.2f%n", getTotalDailyDeposits());
    }
}