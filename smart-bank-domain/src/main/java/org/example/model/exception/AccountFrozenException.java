package org.example.model.exception;

import org.example.model.AccountStatus;

public class AccountFrozenException extends AccountException {

    public final AccountStatus status;

    public AccountFrozenException(String accountNumber,AccountStatus status) {
        super("Account is frozen: " + accountNumber,
                "ACC_FROZEN", accountNumber);
        this.status = status;
    }
    public AccountStatus getStatus() {
        return status;
    }
}
