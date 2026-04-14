package org.example.model;

// Extends RuntimeException → UNCHECKED (caller isn't forced to catch)
// We chose unchecked because insufficient funds is a business logic issue,
// not an external system failure
public class InsufficientFundsException extends RuntimeException {
    private final double requested;
    private final double available;
    public InsufficientFundsException(double requested, double available) {
        super(String.format("Insufficient funds: requested $%.2f, but only $%.2f available.",
                requested, available));
        this.requested = requested;
        this.available = available;
    }

    public double getRequested() {
        return requested;
    }
    public double getAvailable() {
        return available;
    }
    public double getShortfall(){
        return requested - available;
    }
}
