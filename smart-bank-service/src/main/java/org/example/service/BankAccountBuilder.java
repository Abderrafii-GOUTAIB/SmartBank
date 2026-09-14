package org.example.service;

import org.example.model.AccountType;
import org.example.model.AccountTypeChoice;
import org.example.model.BankAccount;
import org.example.model.BankConfig;
import org.example.model.exception.AccountException;
import org.example.model.exception.InvalidAmountException;

import java.util.Optional;

public class BankAccountBuilder {

    // ── REQUIRED FIELDS ───────────────────────────────────────────────────────
    private String ownerName;
    private String email;
    private AccountType accountType;

    // ── OPTIONAL FIELDS ───────────────────────────────────────────────────────
    // Optional<T> from Step 27 — explicit about what can be absent
    private double initialDeposit = 0.0;
    private Optional<String> referralCode = Optional.empty();
    private Optional<String> branchCode = Optional.empty();

    // ── REQUIRED SETTERS — return this for chaining ───────────────────────────

    public BankAccountBuilder name(String ownerName) {
        this.ownerName = ownerName;
        return this;
    }

    public BankAccountBuilder email(String email) {
        this.email = email == null ? "" : email.toLowerCase().trim();
        return this;
    }

    public BankAccountBuilder type(AccountType accountType) {
        this.accountType = accountType;
        return this;
    }

    // convenience — accept enum directly (AccountTypeChoice from Step 39)
    public BankAccountBuilder type(AccountTypeChoice choice) {
        this.accountType = AccountTypeFactory.create(choice.menuIndex());
        return this;
    }

    // ── OPTIONAL SETTERS ──────────────────────────────────────────────────────

    public BankAccountBuilder deposit(double initialDeposit) {
        this.initialDeposit = initialDeposit;
        return this;
    }

    public BankAccountBuilder referralCode(String referralCode) {
        // Optional.ofNullable — safe even if null passed (Step 27)
        this.referralCode = Optional.ofNullable(referralCode)
                .filter(s -> !s.isBlank());
        return this;
    }

    public BankAccountBuilder branchCode(String branchCode) {
        this.branchCode = Optional.ofNullable(branchCode)
                .filter(s -> !s.isBlank());
        return this;
    }

    // ── BUILD ─────────────────────────────────────────────────────────────────
    // Single place where ALL validation runs before object creation.
    // Object only exists if it's 100% valid.

    public BankAccount build() {
        validate();

        BankAccount account = new BankAccount(ownerName, email,
                0.0, accountType);

        if (initialDeposit > 0) {
            account.deposit(initialDeposit);  // ← creates a real Transaction
        }

        // log optional fields if present — method reference + Optional (Step 27)
        referralCode.ifPresent(code ->
                System.out.println("  📋 Referral code applied: " + code));
        branchCode.ifPresent(code ->
                System.out.println("  🏦 Branch: " + code));

        return account;
    }

    // ── VALIDATION ────────────────────────────────────────────────────────────
    // private — only build() calls this
    // uses your exception hierarchy (Step 20)

    private void validate() {
        if (ownerName == null || ownerName.isBlank()) {
            throw new AccountException(
                    "Owner name is required",
                    "ACC_MISSING_NAME", null);
        }
        if (accountType == null) {
            throw new AccountException(
                    "Account type is required",
                    "ACC_MISSING_TYPE", null);
        }
        if (initialDeposit < BankConfig.getInstance().getMinimumOpeningDeposit()) {
            throw new InvalidAmountException(null, initialDeposit);
        }
        // email format — streams + Optional chain (Phase 3)
        Optional.ofNullable(email)
                .filter(e -> !e.isBlank())
                .filter(e -> !e.contains("@"))
                .ifPresent(e -> {
                    throw new AccountException(
                            "Invalid email format: " + e,
                            "ACC_INVALID_EMAIL", null);
                });
    }

    // ── STATIC FACTORY — convenience entry point ──────────────────────────────
    // Instead of: new BankAccountBuilder().name(...).email(...)
    // You can:    BankAccountBuilder.of("Abderrafii", "a@b.com")

    public static BankAccountBuilder of(String name, String email) {
        return new BankAccountBuilder().name(name).email(email);
    }
}