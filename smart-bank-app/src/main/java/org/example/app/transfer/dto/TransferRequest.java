package org.example.app.transfer.dto;


public record TransferRequest(
        String fromAccountNumber,
        String toAccountNumber,
        double amount
) {}

