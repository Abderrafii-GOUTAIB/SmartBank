package org.example.model.exception;

public class TransactionException extends BankException {

    private final String accountNumber;
    private final double amount;

    public TransactionException(String message, String errorCode,
                                String accountNumber, double amount) {
        super(message, errorCode);
        this.accountNumber = accountNumber;
        this.amount = amount;
    }

    public String getAccountNumber() { return accountNumber; }
    public double getAmount()        { return amount; }
}