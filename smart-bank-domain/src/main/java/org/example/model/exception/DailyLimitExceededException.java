package org.example.model.exception;

public class DailyLimitExceededException extends TransactionException {

    private final double dailyLimit;
    private final double alreadyUsed;

    public DailyLimitExceededException(String accountNumber, double amount,
                                       double dailyLimit, double alreadyUsed) {
        super(String.format("Daily limit exceeded: tried $%.2f, limit $%.2f, already used $%.2f",
                        amount, dailyLimit, alreadyUsed),
                "TXN_DAILY_LIMIT",
                accountNumber, amount);
        this.dailyLimit = dailyLimit;
        this.alreadyUsed = alreadyUsed;
    }

    public double getDailyLimit()  { return dailyLimit; }
    public double getAlreadyUsed() { return alreadyUsed; }
    public double getRemaining()   { return dailyLimit - alreadyUsed; }
}