package org.example.service;

import org.example.model.AccountType;
import org.example.model.AccountTypeChoice;

import java.util.Arrays;
import java.util.List;

public class AccountTypeFactory {
    public static AccountType create(int choice) {
        return create(AccountTypeChoice.fromIndex(choice));
    }

    public static AccountType create(String typeName) {
        return create(AccountTypeChoice.fromName(typeName));
    }

    private static AccountType create(AccountTypeChoice choice) {
        return switch (choice) {
            case SAVINGS    -> new AccountType.Savings();
            case CHECKING   -> new AccountType.Checking();
            case BUSINESS   -> new AccountType.Business();
            case INVESTMENT -> new AccountType.Investment();
            case PREMIUM    -> new AccountType.Premium();
        };

    }

    public static List<String> getMenuLines() {
        return Arrays.stream(AccountTypeChoice.values())
                .map(AccountTypeChoice::menuLine)
                .toList();
    }
}
