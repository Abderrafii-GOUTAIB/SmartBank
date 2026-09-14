package org.example.model;


@FunctionalInterface
public interface InterestStrategy {
    double calculate(double balance);
}
