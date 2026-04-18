package org.example.model;

public enum TransactionType {

    // Each constant is like calling new TransactionType("Deposit", +1)
    DEPOSIT("Deposit", +1),
    WITHDRAWAL("Withdrawal", -1),
    TRANSFER_IN("Transfer In", +1),
    TRANSFER_OUT("Transfer Out", -1),
    INTEREST("Interest", +1),
    FEE("Fee", -1);

    // Enums can have fields — just like a class
    private final String displayName;
    private final int sign; // +1 = money in, -1 = money out

    // Private constructor — you can NEVER call new TransactionType() from outside
    // The only instances are the constants defined above
    TransactionType(String displayName, int sign) {
        this.displayName = displayName;
        this.sign = sign;
    }

    // Methods — just like a class
    public String displayName() { return displayName; }
    public int sign()           { return sign; }

    // Is this a credit (money coming in)?
    public boolean isCredit() { return sign > 0; }

    // Is this a debit (money going out)?
    public boolean isDebit() { return sign < 0; }

    // Format amount with the correct sign
    public String formatAmount(double amount) {
        String prefix = isCredit() ? "+" : "-";
        return String.format("%s$%.2f", prefix, amount);
    }

    // Reverse lookup — find enum from a display name string
    // Useful when reading data from files or user input
    public static TransactionType fromString(String text) {
        for (TransactionType type : values()) {
            // values() returns ALL constants as an array
            // [DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT, INTEREST, FEE]
            if (type.name().equalsIgnoreCase(text)
                    || type.displayName.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown transaction type: " + text);
    }
}