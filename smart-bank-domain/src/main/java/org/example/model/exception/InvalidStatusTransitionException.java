package org.example.model.exception;

import org.example.model.AccountStatus;

public class InvalidStatusTransitionException extends AccountException {

    private final AccountStatus from;
    private final AccountStatus to;

    public InvalidStatusTransitionException(String accountNumber,
                                            AccountStatus from,
                                            AccountStatus to) {
        super("Cannot transition account " + accountNumber
                        + " from " + from + " to " + to,
                "ACC_INVALID_TRANSITION",
                accountNumber);
        this.from = from;
        this.to = to;
    }

    public AccountStatus getFrom() { return from; }
    public AccountStatus getTo()   { return to; }
}