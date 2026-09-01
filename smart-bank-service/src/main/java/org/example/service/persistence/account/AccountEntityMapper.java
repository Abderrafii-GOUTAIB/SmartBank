// FILE: smart-bank-service/src/main/java/org/example/service/persistence/AccountEntityMapper.java   [NEW]

package org.example.service.persistence.account;

import org.example.model.*;
import org.example.service.AccountTypeFactory;
import org.example.service.persistence.transaction.TransactionEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Maps between the domain BankAccount and the JPA AccountEntity.
 * Keeps the domain pure and the entity persistence-focused.
 */
@Component
public class AccountEntityMapper {

    // domain → entity (for saving)
    public AccountEntity toEntity(BankAccount account) {
        AccountEntity entity = new AccountEntity(
                account.getAccountNumber(),
                account.getOwnerName(),
                account.getEmail(),
                account.getBalance(),
                account.getAccountType().displayName(),        // sealed → String
                account.getStatus(),                    // your real enum
                account.getCreatedAt()
        );

        for (Transaction t : account.getTransactions()) {
            entity.addTransaction(new TransactionEntity(
                    t.getType().name(),
                    BigDecimal.valueOf(t.getAmount()),
                    t.getDateTime(),
                    entity,
                    t.getBalanceAfter()
            ));
        }
        return entity;
    }

    // entity → domain (for loading)
    public BankAccount toDomain(AccountEntity entity) {
        // rebuild the sealed AccountType from the stored String
        AccountType accountType = AccountTypeFactory.create(entity.getAccountType());

        BankAccount account = BankAccount.rehydrate(
                entity.getAccountNumber(),
                entity.getOwnerName(),
                entity.getEmail(),
                entity.getBalance(),
                accountType,
                entity.getStatus(),
                entity.getCreatedAt()
        );

        for (TransactionEntity t : entity.getTransactions()) {
            account.restoreTransaction(
                    TransactionType.valueOf(t.getTransactionType()),
                    t.getAmount().doubleValue(),
                    t.getBalanceAfter(),
                    t.getDateTime()
            );
        }

        return account;
    }
}