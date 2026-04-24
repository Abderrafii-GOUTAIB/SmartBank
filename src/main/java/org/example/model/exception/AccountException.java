package org.example.model.exception;

public class AccountException extends BankException {

    private  final String accountNumber;

    public AccountException(String message, String errorCode, String accountNumber) {
        super(message, errorCode);
        this.accountNumber = accountNumber;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
}
