package org.example.model;

// ================================================
// RECORD AS VALUE OBJECT — wraps a primitive with meaning
// ================================================
// Instead of passing raw 'double' everywhere (is it dollars? cents? euros?),
// MoneyAmount makes the unit explicit and adds behavior

public record MoneyAmount(double value, String currency) {

    // Compact constructor — validation
    public MoneyAmount {
        if (currency == null || currency.isBlank()) {
            currency = "USD";
        }
        currency = currency.toUpperCase();
    }

    // Convenience constructor — USD by default
    public MoneyAmount(double value) {
        this(value, "USD");
    }

    // ================================================
    // OPERATIONS — return NEW records (immutable!)
    // ================================================
    // Records are immutable — operations create NEW objects

    public MoneyAmount add(MoneyAmount other) {
        validateSameCurrency(other);
        return new MoneyAmount(this.value + other.value, this.currency);
    }

    public MoneyAmount subtract(MoneyAmount other) {
        validateSameCurrency(other);
        return new MoneyAmount(this.value - other.value, this.currency);
    }

    public MoneyAmount multiply(double factor) {
        return new MoneyAmount(this.value * factor, this.currency);
    }

    public boolean isPositive() { return value > 0; }
    public boolean isNegative() { return value < 0; }
    public boolean isZero()     { return value == 0; }

    // ================================================
    // FORMATTING
    // ================================================

    public String formatted() {
        return String.format("$%,.2f %s", value, currency);
    }

    public String formattedShort() {
        return String.format("$%,.2f", value);
    }

    // ================================================
    // PRIVATE HELPERS
    // ================================================

    private void validateSameCurrency(MoneyAmount other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot mix currencies: " + this.currency + " and " + other.currency);
        }
    }

    // ================================================
    // RECORDS AUTO-GENERATE: equals, hashCode, toString
    // ================================================
    // Two MoneyAmount records are equal if value AND currency match:
    //   new MoneyAmount(100, "USD").equals(new MoneyAmount(100, "USD")) → true
    //   new MoneyAmount(100, "USD").equals(new MoneyAmount(100, "EUR")) → false
    //
    // toString: "MoneyAmount[value=100.0, currency=USD]"
}