package org.example.model;

import org.example.model.exception.AccountFrozenException;
import org.example.model.exception.InsufficientFundsException;
import org.example.model.exception.InvalidAmountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Behaviour tests for BankAccount, written in Given/When/Then style.
 * Method names read as: should<Outcome>_when<Condition>
 */
@DisplayName("BankAccount")
class BankAccountTest {

    private BankAccount account;

    @BeforeEach
    void setUp() {
        // GIVEN — a fresh savings account with $1000, for every test
        account = new BankAccount("Abderrafii", "a@b.com", 1000,
                new AccountType.Savings());
    }

    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("deposit")
    class Deposit {

        @Test
        @DisplayName("should increase the balance when the amount is valid")
        void shouldIncreaseBalance_whenAmountIsValid() {
            // GIVEN — an account with $1000 (from setUp)

            // WHEN — $500 is deposited
            account.deposit(500);

            // THEN — the balance is $1500
            assertEquals(1500, account.getBalance());
        }

        @Test
        @DisplayName("should record a transaction when a deposit succeeds")
        void shouldRecordTransaction_whenDepositSucceeds() {
            // GIVEN
            int transactionsBefore = account.getTransactionCount();

            // WHEN
            account.deposit(500);

            // THEN
            assertEquals(transactionsBefore + 1, account.getTransactionCount());
        }

        @Test
        @DisplayName("should throw InvalidAmountException when the amount is zero")
        void shouldThrowInvalidAmount_whenAmountIsZero() {
            // GIVEN — an active account

            // WHEN / THEN — depositing zero is rejected
            assertThrows(InvalidAmountException.class,
                    () -> account.deposit(0));
        }

        @Test
        @DisplayName("should throw InvalidAmountException when the amount is negative")
        void shouldThrowInvalidAmount_whenAmountIsNegative() {
            // GIVEN — an active account

            // WHEN / THEN — depositing a negative amount is rejected
            assertThrows(InvalidAmountException.class,
                    () -> account.deposit(-100));
        }

        @Test
        @DisplayName("should throw AccountFrozenException when the account is frozen")
        void shouldThrowAccountFrozen_whenAccountIsFrozen() {
            // GIVEN — the account is frozen
            account.changeStatus(AccountStatus.FROZEN);

            // WHEN / THEN — deposits are blocked
            assertThrows(AccountFrozenException.class,
                    () -> account.deposit(500));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("withdraw")
    class Withdraw {

        @Test
        @DisplayName("should decrease the balance when funds are sufficient")
        void shouldDecreaseBalance_whenFundsAreSufficient() {
            // GIVEN — an account with $1000

            // WHEN — $300 is withdrawn
            account.withdraw(300);

            // THEN — the balance is $700
            assertEquals(700, account.getBalance());
        }

        @Test
        @DisplayName("should throw InsufficientFundsException when funds are too low")
        void shouldThrowInsufficientFunds_whenFundsAreTooLow() {
            // GIVEN — an account with $1000

            // WHEN — withdrawing more than the balance
            InsufficientFundsException thrown = assertThrows(
                    InsufficientFundsException.class,
                    () -> account.withdraw(5000));

            // THEN — the exception carries the correct details
            assertEquals(5000, thrown.getRequested());
            assertEquals(1000, thrown.getAvailable());
        }

        @Test
        @DisplayName("should leave the balance unchanged when the withdrawal fails")
        void shouldLeaveBalanceUnchanged_whenWithdrawalFails() {
            // GIVEN — an account with $1000

            // WHEN — an over-limit withdrawal is attempted
            assertThrows(InsufficientFundsException.class,
                    () -> account.withdraw(1001));

            // THEN — no money moved
            assertEquals(1000, account.getBalance());
        }

        @Test
        @DisplayName("should throw AccountFrozenException when the account is frozen")
        void shouldThrowAccountFrozen_whenAccountIsFrozen() {
            // GIVEN — the account is frozen
            account.changeStatus(AccountStatus.FROZEN);

            // WHEN / THEN — withdrawals are blocked
            assertThrows(AccountFrozenException.class,
                    () -> account.withdraw(100));
        }

        @Test
        @DisplayName("should throw InvalidAmountException when the amount is negative")
        void shouldThrowInvalidAmount_whenAmountIsNegative() {
            // GIVEN — an active account

            // WHEN / THEN
            assertThrows(InvalidAmountException.class,
                    () -> account.withdraw(-50));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("equality")
    class Equality {

        @Test
        @DisplayName("should not be equal when the account numbers differ")
        void shouldNotBeEqual_whenAccountNumbersDiffer() {
            // GIVEN — a second, different account
            BankAccount other = new BankAccount("Bob", "b@c.com", 999,
                    new AccountType.Checking());

            // WHEN / THEN — different account numbers → not equal
            assertNotEquals(account, other);
        }

        @Test
        @DisplayName("should be equal to itself and have a stable hashCode")
        void shouldBeEqualToItself_andHaveStableHashCode() {
            // GIVEN — the same account instance

            // WHEN / THEN — reflexive equality and consistent hashCode
            assertEquals(account, account);
            assertEquals(account.hashCode(), account.hashCode());
        }
    }
}