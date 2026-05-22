package org.example.service;

public class InterestStrategies {
    private InterestStrategies() {
    }

    public static InterestStrategy simple(double rate) {
        return balance -> balance * rate;
    }

    public static InterestStrategy compound(double rate) {
        return balance -> balance * (Math.pow(1 + rate / 12, 12) - 1);
    }

    public static InterestStrategy tiered(double lowRate, double midRate, double highRate) {
        return balance -> {
            if (balance < 1_000) return balance * lowRate;
            if (balance < 10_000) return balance * midRate;
            return balance * highRate;
        };
    }

    public static InterestStrategy ZERO = balance -> 0.0;

    public static InterestStrategy promotional(double rate) {
        return balance -> balance * rate * 2;
    }
}
