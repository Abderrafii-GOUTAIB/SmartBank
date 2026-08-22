package org.example.app.api.dto;

public record OpenAccountRequest(
        String ownerName,
        String email,
        Double initialDeposit,
        String accountType
) {
}
