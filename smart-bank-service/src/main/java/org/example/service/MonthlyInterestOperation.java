package org.example.service;

import org.example.model.BankAccount;
import org.example.model.BankEvent;
import org.example.model.TransactionType;

import java.util.function.Consumer;

public class MonthlyInterestOperation extends MonthlyOperation {

    public MonthlyInterestOperation(AccountRepository repository, Consumer<BankEvent> publisher) {
        super(repository, publisher);
    }

    @Override
    public String operationName() {
        return "Monthly Interest";
    }

    @Override
    public double getAmount(BankAccount account) {
        return account.getAccountType().interestStrategy().calculate(account.getBalance());
    }

    @Override
    public void apply(BankAccount account, double amount) {
        account.deposit(amount);
    }

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.INTEREST;
    }
}
