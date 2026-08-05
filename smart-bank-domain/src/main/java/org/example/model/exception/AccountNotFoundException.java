package org.example.model.exception;

public class AccountNotFoundException extends AccountException {

    public AccountNotFoundException(String accountNumber) {
        super("Account not found: " + accountNumber,
                "ACC_NOT_FOUND", accountNumber);
    }

}
