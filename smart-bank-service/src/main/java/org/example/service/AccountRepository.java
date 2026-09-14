package org.example.service;

import org.example.model.BankAccount;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * DIP seam: AccountService (and every other consumer) depends on THIS,
 * not on a specific storage implementation.
 * InMemoryAccountRepository implements it today. JpaAccountRepository
 * will too (Step 15).
 */
public interface AccountRepository {

    void save(BankAccount account);
    void remove(BankAccount account);

    BankAccount getByAccountNumber(String accountNumber);
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    BankAccount getByIndex(int index);
    List<BankAccount> searchByName(String searchTerm);

    boolean emailExists(String email);
    boolean accountExists(String accountNumber);
    boolean ownerNameExists(String ownerName);

    int count();
    boolean isEmpty();

    List<BankAccount> getAll();
    List<BankAccount> getAllSorted(Comparator<BankAccount> comparator);

    void clear();
    void saveAll(List<BankAccount> loaded);
}