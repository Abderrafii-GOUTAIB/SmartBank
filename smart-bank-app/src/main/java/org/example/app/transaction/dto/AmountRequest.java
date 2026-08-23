package org.example.app.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AmountRequest(
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        Double amount
) {}