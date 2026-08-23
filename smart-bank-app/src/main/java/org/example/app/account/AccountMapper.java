package org.example.app.account;

import org.example.app.account.dto.AccountResponse;
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

