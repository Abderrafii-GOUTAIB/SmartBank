package org.example.service;


public class TransferCommand implements BankCommand {

    private final AccountService service;
    private final String fromAccNum;
    private final String toAccNum;
    private final double amount;
    private boolean executed = false;

    public TransferCommand(AccountService service,
                           String fromAccNum,
                           String toAccNum,
                           double amount) {
        this.service    = service;
        this.fromAccNum = fromAccNum;
        this.toAccNum   = toAccNum;
        this.amount     = amount;
    }

    @Override
    public void execute() {
        service.transfer(fromAccNum, toAccNum, amount);
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            System.out.println("  ⚠️  Cannot undo — command never executed.");
            return;
        }
        // undo transfer = transfer back the other way
        service.transfer(toAccNum, fromAccNum, amount);
        executed = false;
    }

    @Override
    public String description() {
        return String.format("TRANSFER $%,.2f from %s to %s",
                amount, fromAccNum, toAccNum);
    }
}