package org.example.app.account.dto;

/**
 * Response DTO — what the API EXPOSES about an account.
 * A record → immutable (Phase 3 principle).
 * We do NOT return the domain BankAccount directly:
 *   → decouples the API from the domain model
 *   → hides internal state/behavior
 *   → lets API and domain evolve independently (SRP + clean layering)
 */


public record AccountResponse (
        String accountNumber,
        String ownerName,
        double balance,
        String accountType,
        String status
){}

