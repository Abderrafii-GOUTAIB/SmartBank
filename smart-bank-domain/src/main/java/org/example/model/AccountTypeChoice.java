package org.example.model;

import org.example.model.exception.AccountException;

public enum AccountTypeChoice {
    SAVINGS("Savings", "4.5% interest, no fee"),
    CHECKING("Checking", "0.1% interest, $4.99/month fee"),
    BUSINESS("Business", "2.0% interest, $29.99/month fee"),
    INVESTMENT("Investment", "3.5% interest, $49.99/month"),
    PREMIUM("Premium", "6.0% interest, $99.99/month fee");


    private final String displayName;
    private final String description;

    AccountTypeChoice(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public int menuIndex() {
        return ordinal() + 1;
    }

    public String displayName() {
        return displayName;
    }

    public String menuLine() {
        return String.format("   %d. %-12s (%s)", menuIndex(), displayName(), description);
    }

    public static AccountTypeChoice fromIndex(int index) {
        for (AccountTypeChoice c : values()) {
            if (c.menuIndex() == index) return c;
        }
        throw new AccountException(
                "Invalid account type selection: " + index,
                "INVALID_ACCOUNT_TYPE", null);
    }

    public static AccountTypeChoice fromName(String name) {
        for (AccountTypeChoice c : values()) {
            if (c.displayName.equalsIgnoreCase(name.trim())) return c;
        }
        throw new AccountException(
                "Invalid account type name: " + name,
                "INVALID_ACCOUNT_TYPE", null);
    }
}
