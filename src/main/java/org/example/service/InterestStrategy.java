package org.example.service;


@FunctionalInterface
public interface InterestStrategy {
    double calculate(double balance);
}
