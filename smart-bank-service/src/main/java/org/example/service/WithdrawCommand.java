package org.example.service;

import org.example.model.BankAccount;


public class WithdrawCommand implements BankCommand {

    private final AccountService service;
    private final BankAccount account;
    private final double amount;
    private boolean executed = false;

    public WithdrawCommand(AccountService service,
                           BankAccount account,
                           double amount) {
        this.service = service;
        this.account = account;
        this.amount  = amount;
    }

    @Override
    public void execute() {
        service.withdraw(account, amount);
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            System.out.println("  ⚠️  Cannot undo — command never executed.");
            return;
        }
        service.deposit(account, amount);
        executed = false;
    }

    @Override
    public String description() {
        return String.format("WITHDRAW $%,.2f on %s",
                amount, account.getAccountNumber());
    }
}