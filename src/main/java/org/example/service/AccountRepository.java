package org.example.service;

import org.example.model.BankAccount;
import org.example.model.exception.AccountNotFoundException;

import java.util.*;

public class AccountRepository {

    private final List<BankAccount> accounts;
    private final Map<String, BankAccount> accountMap;
    private final Set<String> registeredEmails;
    private final Set<String> registeredOwnerNames;

    public AccountRepository() {
        this.accounts = new ArrayList<>();
        this.accountMap = new HashMap<>();
        this.registeredEmails = new HashSet<>();
        this.registeredOwnerNames = new HashSet<>();
    }

    // --- Store ---

    public void save(BankAccount account) {
        accounts.add(account);
        accountMap.put(account.getAccountNumber(), account);
        Optional.ofNullable(account.getOwnerName()).map(String::trim).filter(s -> !s.isBlank()).ifPresent(s -> registeredOwnerNames.add(s.toLowerCase()));
        Optional.ofNullable(account.getEmail()).map(String::trim).filter(s -> !s.isBlank()).ifPresent(s -> registeredEmails.add(s.toLowerCase()));
    }

    public void remove(BankAccount account) {
        accounts.remove(account);
        accountMap.remove(account.getAccountNumber());
        registeredEmails.remove(account.getEmail());
        registeredOwnerNames.remove(account.getOwnerName());
    }

    // --- Find ---

    public BankAccount findByAccountNumber(String accountNumber) {
        BankAccount account = accountMap.get(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        return account;
    }

    public Optional<BankAccount> findByAccountNumberSafe(String accountNumber) {
        return Optional.ofNullable(accountMap.get(accountNumber));
    }

    public BankAccount getByIndex(int index) {
        if (index < 0 || index >= accounts.size()) {
            throw new IndexOutOfBoundsException("Invalid index: " + (index + 1));
        }
        return accounts.get(index);
    }

    public List<BankAccount> searchByName(String searchTerm) {
        return accounts.stream().filter(acc -> acc.getOwnerName().toLowerCase().contains(searchTerm.toLowerCase())).toList();
    }

    // --- Query ---

    public boolean emailExists(String email) {
        return registeredEmails.contains(email.toLowerCase().trim());
    }

    public boolean accountExists(String accountNumber) {
        return accountMap.containsKey(accountNumber);
    }

    public boolean ownerNameExists(String ownerName) {
        return registeredOwnerNames.contains(ownerName.toLowerCase().trim());
    }

    public int count() {
        return accounts.size();
    }

    public boolean isEmpty() {
        return accounts.isEmpty();
    }

    public List<BankAccount> getAll() {
        return List.copyOf(accounts);
    }

    public List<BankAccount> getAllSorted(Comparator<BankAccount> comparator) {
        return accounts.stream().sorted(comparator).toList();
    }

    // --- Bulk operations (used by DataStore for loading) ---

    public void clear() {
        accounts.clear();
        accountMap.clear();
        registeredEmails.clear();
    }

    public void saveAll(List<BankAccount> loaded) {
        for (BankAccount acc : loaded) {
            save(acc);
        }
    }
}