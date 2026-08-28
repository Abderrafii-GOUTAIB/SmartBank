package org.example.service;

import org.example.model.BankAccount;
import org.example.model.exception.AccountNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryAccountRepository implements AccountRepository {

    private final List<BankAccount> accounts = new CopyOnWriteArrayList<>();
    private final Map<String, BankAccount> accountMap = new ConcurrentHashMap<>();
    private final Set<String> registeredEmails = ConcurrentHashMap.newKeySet();
    private final Set<String> registeredOwnerNames = ConcurrentHashMap.newKeySet();

    public InMemoryAccountRepository() {
    }

    @Override
    public void save(BankAccount account) {
        accounts.add(account);
        accountMap.put(account.getAccountNumber(), account);
        Optional.ofNullable(account.getOwnerName()).map(String::trim).filter(s -> !s.isBlank()).ifPresent(s -> registeredOwnerNames.add(s.toLowerCase()));
        Optional.ofNullable(account.getEmail()).map(String::trim).filter(s -> !s.isBlank()).ifPresent(s -> registeredEmails.add(s.toLowerCase()));
    }

    @Override
    public void remove(BankAccount account) {
        accounts.remove(account);
        accountMap.remove(account.getAccountNumber());
        Optional.ofNullable(account.getEmail()).map(String::trim).filter(s -> !s.isBlank()).ifPresent(s -> registeredEmails.remove(s.toLowerCase()));
        Optional.ofNullable(account.getOwnerName()).map(String::trim).filter(s -> !s.isBlank()).ifPresent(s -> registeredOwnerNames.remove(s.toLowerCase()));
    }

    @Override
    public BankAccount getByAccountNumber(String accountNumber) {
        BankAccount account = accountMap.get(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        return account;
    }

    @Override
    public Optional<BankAccount> findByAccountNumber(String accountNumber) {
        return Optional.ofNullable(accountMap.get(accountNumber));
    }

    @Override
    public BankAccount getByIndex(int index) {
        if (index < 0 || index >= accounts.size()) {
            throw new IndexOutOfBoundsException("Invalid index: " + (index + 1));
        }
        return accounts.get(index);
    }

    @Override
    public List<BankAccount> searchByName(String searchTerm) {
        return accounts.stream().filter(acc -> acc.getOwnerName().toLowerCase().contains(searchTerm.toLowerCase())).toList();
    }

    @Override
    public boolean emailExists(String email) {
        return registeredEmails.contains(email.toLowerCase().trim());
    }

    @Override
    public boolean accountExists(String accountNumber) {
        return accountMap.containsKey(accountNumber);
    }

    @Override
    public boolean ownerNameExists(String ownerName) {
        return registeredOwnerNames.contains(ownerName.toLowerCase().trim());
    }

    @Override
    public int count() {
        return accounts.size();
    }

    @Override
    public boolean isEmpty() {
        return accounts.isEmpty();
    }

    @Override
    public List<BankAccount> getAll() {
        return List.copyOf(accounts);
    }

    @Override
    public List<BankAccount> getAllSorted(Comparator<BankAccount> comparator) {
        return accounts.stream().sorted(comparator).toList();
    }

    @Override
    public void clear() {
        accounts.clear();
        accountMap.clear();
        registeredEmails.clear();
        registeredOwnerNames.clear();
    }

    @Override
    public void saveAll(List<BankAccount> loaded) {
        for (BankAccount acc : loaded) {
            save(acc);
        }
    }
}