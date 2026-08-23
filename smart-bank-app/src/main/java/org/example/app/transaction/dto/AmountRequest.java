package org.example.app.transaction.dto;

/**
 * Request DTO for deposit/withdraw — just an amount.
 * A record → immutable input.
 */
public record AmountRequest(
        double amount
) {}

