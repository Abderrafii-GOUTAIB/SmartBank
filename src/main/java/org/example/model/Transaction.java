package org.example.model;

public class Transaction {
    private static int nextId = 1;
    private final int transactionId;
    private final String accountNumber;
    private final TransactionType type;
    private final double amount;
    private final double balanceAfter;
    private final long timestamp;

    public Transaction( String accountNumber, TransactionType type, double amount, double balanceAfter) {
        this.transactionId = nextId++;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = System.currentTimeMillis();
    }

    public static Transaction fromString(String data, String accountNumber) {

        String[] parts = data.split("\\|");

        int partitionId = Integer.parseInt(parts[0]);
        TransactionType parsedType = TransactionType.fromString(parts[1]);
        double parsedAmount = Double.parseDouble(parts[2]);
        double parsedBalance = Double.parseDouble(parts[3]);
        return new Transaction(accountNumber, parsedType, parsedAmount, parsedBalance);
    }

    public String ToDataString() {
        return String.join("|",
                String.valueOf(transactionId),
                type.name(),
                String.valueOf(amount),
                String.valueOf(balanceAfter)
        );
    }

    public Integer getId() {
        return transactionId;
    }

    public int compareAmountTo(Transaction other){
        return Double.compare(amount, other.amount);
    }

    public boolean isAmountBetween(double min, double max){
        if(Double.isNaN(min) || Double.isNaN(max)){
            throw new IllegalArgumentException("Min and Max cannot be NaN");
        }
        return amount >= min && amount <= max;
    }

    // --- Standard getters ---

    public String getAccountNumber() { return accountNumber; }
    public TransactionType getType()          { return type; }
    public String getTypeName() { return type.displayName(); }
    public double getAmount()        { return amount; }
    public double getBalanceAfter()  { return balanceAfter; }
    public long getTimestamp()       { return timestamp; }

    @Override
    public String toString() {
        return String.format("Transaction #%d | %s: $%.2f | Balance After: $%.2f | Time: %s",
                transactionId, type, amount, balanceAfter, new java.util.Date(timestamp));
    }

}
