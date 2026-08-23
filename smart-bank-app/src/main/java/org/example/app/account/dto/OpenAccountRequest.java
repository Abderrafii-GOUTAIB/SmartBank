package org.example.app.account.dto;

public record OpenAccountRequest(
        String ownerName,
        String email,
        Double initialDeposit,
        String accountType
) {
}

