package org.example.service;

import org.example.model.AccountType;
import org.example.model.BankAccount;
import org.example.model.exception.DuplicateEmailException;
import org.example.model.exception.InvalidAmountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountService with all dependencies MOCKED.
 * No real files are written. No real repository is used.
 * We test ONLY AccountService's own logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService")
class AccountServiceTest {

    // @Mock → Mockito creates a FAKE version of each dependency
    @Mock private AccountRepository repository;
    @Mock private RiskService riskService;
    @Mock private TransactionTracker tracker;
    @Mock private StoragePort storage;          // ← fake — writes NO files ✅

    private AccountService service;

    @BeforeEach
    void setUp() {
        // GIVEN — a service wired with fakes, not real components
        service = new AccountService(repository, riskService, tracker, storage);
    }

    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("openAccount")
    class OpenAccount {

        @Test
        @DisplayName("should save the account to the repository when details are valid")
        void shouldSaveAccount_whenDetailsAreValid() {
            // GIVEN — no duplicate name or email exists
            when(repository.ownerNameExists(anyString())).thenReturn(false);
            when(repository.emailExists(anyString())).thenReturn(false);

            // WHEN — an account is opened
            BankAccount created = service.openAccount(
                    "Abderrafii", "a@b.com", 5000, new AccountType.Savings());

            // THEN — it was handed to the repository to store
            verify(repository).save(any(BankAccount.class));
            assertNotNull(created);
            assertEquals(5000, created.getBalance());
        }

        @Test
        @DisplayName("should throw DuplicateEmailException when the email is taken")
        void shouldThrowDuplicateEmail_whenEmailIsTaken() {
            // GIVEN — the email already exists in the repository
            when(repository.ownerNameExists(anyString())).thenReturn(false);
            when(repository.emailExists("taken@b.com")).thenReturn(true);

            // WHEN / THEN — opening the account is rejected
            assertThrows(DuplicateEmailException.class, () ->
                    service.openAccount("Bob", "taken@b.com", 5000,
                            new AccountType.Savings()));

            // AND — nothing was saved
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidAmountException when the deposit is below the minimum")
        void shouldThrowInvalidAmount_whenDepositBelowMinimum() {
            // GIVEN — a deposit below the configured minimum

            // WHEN / THEN — rejected before touching the repository
            assertThrows(InvalidAmountException.class, () ->
                    service.openAccount("Bob", "b@c.com", -1,
                            new AccountType.Savings()));

            verify(repository, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("saveAll")
    class SaveAll {

        @Test
        @DisplayName("should delegate persistence to the storage port")
        void shouldDelegateToStoragePort() {
            // GIVEN — the repository returns some accounts
            when(repository.getAll()).thenReturn(java.util.List.of());
            when(riskService.getFlaggedAccounts()).thenReturn(java.util.Set.of());

            // WHEN — saveAll is called
            service.saveAll();

            // THEN — the storage port was asked to persist state
            //        (we do NOT care whether a file was really written —
            //         that is DataStore's own test)
            verify(storage).saveState(any());
            verify(storage).saveFlaggedAccounts(any());
        }

        @Test
        @DisplayName("should mark the service clean after a successful save")
        void shouldMarkClean_afterSuccessfulSave() {
            // GIVEN — a dirty service (an account was opened)
            when(repository.ownerNameExists(anyString())).thenReturn(false);
            when(repository.emailExists(anyString())).thenReturn(false);
            when(repository.getAll()).thenReturn(java.util.List.of());
            when(riskService.getFlaggedAccounts()).thenReturn(java.util.Set.of());

            service.openAccount("Abder", "a@b.com", 5000, new AccountType.Savings());
            assertTrue(service.isDirty(), "opening an account should mark it dirty");

            // WHEN — everything is saved
            service.saveAll();

            // THEN — there are no unsaved changes
            assertFalse(service.isDirty());
        }
    }
}