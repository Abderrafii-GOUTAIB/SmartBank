package org.example.app.transaction.dto;


public record TransactionResponse(
        String accountNumber,
        double amount,
        double newBalance,
        String type,
        boolean success,
        String message
) {}

