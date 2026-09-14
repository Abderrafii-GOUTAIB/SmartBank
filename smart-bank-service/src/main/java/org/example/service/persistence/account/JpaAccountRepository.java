package org.example.service.persistence.account;

import org.example.model.BankAccount;
import org.example.model.exception.AccountNotFoundException;
import org.example.service.AccountRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * JPA-backed implementation of AccountRepository.
 * Delegates the actual persistence to Spring Data (AccountJpaRepository)
 * and translates AccountEntity <-> BankAccount via AccountEntityMapper.
 *
 * NOTE: getByIndex and getAllSorted are in-memory-era operations that
 * don't map cleanly onto a database — see the caveats on each below.
 * Not wired in as the active bean yet (Step 15a's @Primary decision
 * still applies) — this class exists but InMemoryAccountRepository
 * stays the one Spring injects until you say otherwise.
 */
@Primary
@Repository
public class JpaAccountRepository implements AccountRepository {

    private final AccountJpaRepository jpaRepository;
    private final AccountEntityMapper mapper;

    public JpaAccountRepository(AccountJpaRepository jpaRepository, AccountEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(BankAccount account) {
        jpaRepository.save(mapper.toEntity(account));
    }

    @Override
    public void remove(BankAccount account) {
        jpaRepository.deleteById(account.getAccountNumber());
    }

    @Override
    public BankAccount getByAccountNumber(String accountNumber) {
        return jpaRepository.findById(accountNumber)
                .map(mapper::toDomain)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    @Override
    public Optional<BankAccount> findByAccountNumber(String accountNumber) {
        return jpaRepository.findById(accountNumber).map(mapper::toDomain);
    }

    @Override
    public BankAccount getByIndex(int index) {
        // No natural DB equivalent of "the Nth account by insertion order."
        // Falls back to loading everything and indexing in memory — correct,
        // but O(n) per call and not something you'd want at real scale.
        // Flagging this rather than hiding it: worth revisiting once we
        // know whether Main.java's index-based selection can become
        // ID-based instead.
        List<BankAccount> all = getAll();
        if (index < 0 || index >= all.size()) {
            throw new IndexOutOfBoundsException("Invalid index: " + (index + 1));
        }
        return all.get(index);
    }

    @Override
    public List<BankAccount> searchByName(String searchTerm) {
        return jpaRepository.findByOwnerNameContainingIgnoreCase(searchTerm)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean emailExists(String email) {
        return jpaRepository.existsByEmailIgnoreCase(email.trim());
    }

    @Override
    public boolean accountExists(String accountNumber) {
        return jpaRepository.existsById(accountNumber);
    }

    @Override
    public boolean ownerNameExists(String ownerName) {
        return jpaRepository.existsByOwnerNameIgnoreCase(ownerName.trim());
    }

    @Override
    public int count() {
        return Math.toIntExact(jpaRepository.count());
    }

    @Override
    public boolean isEmpty() {
        return count() == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccount> getAll() {
        return jpaRepository.findAllWithTransactions().stream().map(mapper::toDomain).toList();
    }



    @Override
    public List<BankAccount> getAllSorted(Comparator<BankAccount> comparator) {
        // Same issue as getByIndex: an arbitrary Comparator<BankAccount> is
        // pure Java and can't be pushed into a SQL ORDER BY, since the DB
        // only ever sees AccountEntity. Fetch-all-then-sort is correct but
        // defeats the point of letting the database do sorting/paging —
        // this method has no live callers today (verified), so it's a good
        // candidate to drop or redesign once real pagination (Step 13's
        // deferred DB-level pagination) lands in Stage 4.
        return getAll().stream().sorted(comparator).toList();
    }

    @Override
    public void clear() {
        jpaRepository.deleteAll();
    }

    @Override
    public void saveAll(List<BankAccount> loaded) {
        jpaRepository.saveAll(loaded.stream().map(mapper::toEntity).toList());
    }
}