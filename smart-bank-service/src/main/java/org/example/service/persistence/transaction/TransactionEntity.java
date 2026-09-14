package org.example.service.persistence.transaction;

import jakarta.persistence.*;
import org.example.service.persistence.account.AccountEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String transactionType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false)
    private double balanceAfter;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_number", nullable = false)
    private AccountEntity account;

    // JPA requires a no-arg constructor
    protected TransactionEntity() {}

    public TransactionEntity(String transactionType, BigDecimal amount,
                             LocalDateTime dateTime,
                             AccountEntity account, double balanceAfter) {
        this.transactionType = transactionType;
        this.account = account;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.dateTime = dateTime;

    }

    public Long getId() { return id; }
    public String getTransactionType() { return transactionType; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getDateTime() { return dateTime; }
    public AccountEntity getAccount() { return account; }
    public double getBalanceAfter() { return balanceAfter; }
}