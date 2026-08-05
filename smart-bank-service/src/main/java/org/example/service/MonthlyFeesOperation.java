package org.example.service;

import org.example.model.BankAccount;
import org.example.model.BankEvent;
import org.example.model.TransactionType;

import java.util.function.Consumer;

/**
 * Implements the variable steps for fee collection.
 * The skeleton stays in MonthlyOperation — untouched.
 *
 * Overrides shouldProcess() hook — skips accounts with no fee.
 * Overrides onZeroAmount() hook — custom no-fee message.
 */
public class MonthlyFeesOperation extends MonthlyOperation {

    public MonthlyFeesOperation(AccountRepository repository,
                                Consumer<BankEvent> publisher) {
        super(repository, publisher);
    }

    @Override
    protected String operationName() {
        return "Applying Monthly Fees";
    }

    @Override
    protected double getAmount(BankAccount account) {
        return account.getMonthlyFee();
    }

    @Override
    protected void apply(BankAccount account, double amount) {
        account.withdraw(amount);   // fee = money OUT
    }

    @Override
    protected TransactionType getTransactionType() {
        return TransactionType.FEE;
    }


    @Override
    protected boolean shouldProcess(BankAccount account) {
        return account.getMonthlyFee() > 0;
    }

    @Override
    protected void onZeroAmount(BankAccount account) {
        System.out.printf("  %s (%s): no fee ✅%n",
                account.getAccountNumber(),
                account.getAccountType().displayName());
    }
}