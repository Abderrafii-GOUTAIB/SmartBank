package org.example.model.exception;

public class InsufficientFundsException extends TransactionException {

    private final double available;

    public InsufficientFundsException(String accountNumber,
                                      double requested, double available) {
        super(String.format("Insufficient funds: requested $%.2f but only $%.2f available",
                        requested, available),
                "TXN_INSUFFICIENT_FUNDS",
                accountNumber, requested);
        this.available = available;
    }

    public double getRequested() { return getAmount(); }
    public double getAvailable() { return available; }
    public double getShortfall() { return getAmount() - available; }
}