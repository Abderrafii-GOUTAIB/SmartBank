package org.example.app.api.mapper;

import org.example.app.api.dto.AccountResponse;
import org.example.model.BankAccount;
import org.springframework.stereotype.Component;


@Component
public class AccountMapper {
    public AccountResponse toResponse(BankAccount account) {
        return new AccountResponse(
                account.getAccountNumber(),
                account.getOwnerName(),
                account.getBalance(),
                account.getAccountType().displayName(),
                account.getStatus().name()
        );
    }
}
