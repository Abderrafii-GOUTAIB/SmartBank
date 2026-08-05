package org.example.service;

public interface BankCommand {
    void execute();
    void undo();
    String description();
}
