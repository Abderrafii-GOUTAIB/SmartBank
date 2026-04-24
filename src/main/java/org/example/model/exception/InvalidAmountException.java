package org.example.model.exception;

public class InvalidAmountException extends TransactionException {

    public InvalidAmountException(String accountNumber, double amount) {
        super("Invalid amount: $" + String.format("%.2f", amount)
                        + " — must be positive.",
                "TXN_INVALID_AMOUNT",
                accountNumber, amount);
    }
}