package org.example.app.account.dto;

import jakarta.validation.constraints.*;

public record OpenAccountRequest(

        @NotBlank(message = "Owner name is required")
        String ownerName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotNull(message = "Initial deposit is required")
        @Positive(message = "Initial deposit must be positive")
        Double initialDeposit,

        @NotBlank(message = "Account type is required")
        String accountType
) {}