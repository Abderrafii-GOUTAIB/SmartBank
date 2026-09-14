// FILE: smart-bank-service/src/main/java/org/example/service/persistence/AccountEntity.java   [NEW]

package org.example.service.persistence.account;

import jakarta.persistence.*;
import org.example.model.AccountStatus;
import org.example.service.persistence.transaction.TransactionEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity — the DATABASE representation of an account.
 * SEPARATE from the domain BankAccount (which stays pure).
 * This class exists ONLY for persistence:
 * → mutable, no-arg constructor, simple types (JPA needs these)
 * → accountType stored as a String (not the sealed interface)
 */
@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private double balance;

    // accountType stored as its type name: "SAVINGS", "CHECKING"...
    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;   // see note below

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TransactionEntity> transactions = new ArrayList<>();

    // JPA REQUIRES a no-arg constructor (creates objects via reflection)
    protected AccountEntity() {
    }

    // full constructor for the mapper to use
    public AccountEntity(String accountNumber, String ownerName, String email,
                         double balance, String accountType,
                         AccountStatus status, LocalDateTime createdAt) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.email = email;
        this.balance = balance;
        this.accountType = accountType;
        this.status = status;
        this.createdAt = createdAt;
    }

    // getters + setters (JPA needs settable fields)
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String v) {
        this.accountNumber = v;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String v) {
        this.ownerName = v;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String v) {
        this.email = v;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double v) {
        this.balance = v;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String v) {
        this.accountType = v;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus v) {
        this.status = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime v) {
        this.createdAt = v;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void addTransaction(TransactionEntity transaction) {
        transactions.add(transaction);
    }
}