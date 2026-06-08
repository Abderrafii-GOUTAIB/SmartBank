package org.example.service;

import org.example.model.BankAccount;

public class DepositCommand implements BankCommand {

    private final AccountService service;
    private final BankAccount account;
    private final double amount;
    private boolean executed = false;

    public DepositCommand(AccountService service, BankAccount account, double amount) {
        this.service = service;
        this.account = account;
        this.amount = amount;
    }

    @Override
    public void execute() {
        service.deposit(account, amount);
        executed = true;
    }
    @Override
    public void undo() {
        if (!executed) {
            System.out.println("  ⚠️  Cannot undo — command never executed.");
            return;
        }
        // undo deposit = withdraw the same amount
        service.withdraw(account, amount);
        executed = false;
    }

    @Override
    public String description() {
        return String.format("DEPOSIT $%,.2f on %s",
                amount, account.getAccountNumber());
    }
}
