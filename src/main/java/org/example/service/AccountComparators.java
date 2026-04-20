package org.example.service;

import org.example.model.BankAccount;

import java.util.Comparator;

public class AccountComparators {
    public static final Comparator<BankAccount> BY_NAME = Comparator.comparing(BankAccount::getOwnerName);
    public static final Comparator<BankAccount> BY_NAME_DESC = Comparator.comparing(BankAccount::getOwnerName).reversed();
    public static final Comparator<BankAccount> BY_BALANCE = Comparator.comparingDouble(BankAccount::getBalance);
    public static final Comparator<BankAccount> BY_BALANCE_DESC = Comparator.comparingDouble(BankAccount::getBalance).reversed();
    //MOST ACTIVE
    public static final Comparator<BankAccount> BY_ACTIVITY = Comparator.comparing(BankAccount::getTransactionCount).reversed();
    public static final Comparator<BankAccount> BY_ACCOUNT_NUMBER = Comparator.comparing(BankAccount::getAccountNumber);
    // Sort by status: PENDING → ACTIVE → FROZEN → REJECTED → CLOSED
    // .ordinal() returns the enum's position (0, 1, 2, 3, 4)
    // So PENDING (0) comes before ACTIVE (1) comes before FROZEN (2)...
    public static final Comparator<BankAccount> BY_STATUS =
            Comparator.comparingInt(acc -> acc.getStatus().ordinal());

    public static final Comparator<BankAccount> BY_STATUS_THEN_BALANCE =
            Comparator.comparingInt((BankAccount acc) -> acc.getStatus().ordinal())
                    .thenComparing(BY_BALANCE_DESC);
    // Sort by name alphabetically, break ties by balance (highest first)
    public static final Comparator<BankAccount> BY_NAME_THEN_BALANCE =
            Comparator.comparing(BankAccount::getOwnerName)
                    .thenComparing(BY_BALANCE_DESC);

    // ================================================
    // CUSTOM COMPARATOR — written manually (educational)
    // ================================================

    // The manual way — before Comparator.comparing() existed (Java 8)
    // This is what the elegant one-liners above do internally
    public static final Comparator<BankAccount> BY_BALANCE_MANUAL =
            new Comparator<BankAccount>() {
                @Override
                public int compare(BankAccount a, BankAccount b) {
                    // Rule: return negative if a < b, 0 if equal, positive if a > b
                    // Double.compare handles NaN and -0.0 edge cases correctly
                    return Double.compare(a.getBalance(), b.getBalance());
                }
            };

    public static Comparator<BankAccount> byName(String name) {
        return switch (name.toLowerCase()) {
            case "name", "name_asc"          -> BY_NAME;
            case "name_desc"                 -> BY_NAME_DESC;
            case "balance", "balance_asc"    -> BY_BALANCE;
            case "balance_desc"              -> BY_BALANCE_DESC;
            case "activity"                  -> BY_ACTIVITY;
            case "status"                    -> BY_STATUS;
            case "status_balance"            -> BY_STATUS_THEN_BALANCE;
            default -> BY_ACCOUNT_NUMBER;
        };
    }
    public static String describe(Comparator<BankAccount> comp) {
        if (comp == BY_NAME)                return "Name (A → Z)";
        if (comp == BY_NAME_DESC)           return "Name (Z → A)";
        if (comp == BY_BALANCE)             return "Balance (lowest first)";
        if (comp == BY_BALANCE_DESC)        return "Balance (highest first)";
        if (comp == BY_ACTIVITY)            return "Most active first";
        if (comp == BY_ACCOUNT_NUMBER)      return "Account number";
        if (comp == BY_STATUS)              return "Status";
        if (comp == BY_STATUS_THEN_BALANCE) return "Status, then balance";
        return "Custom";
    }

}
