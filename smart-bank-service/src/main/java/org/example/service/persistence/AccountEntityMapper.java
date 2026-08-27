// FILE: smart-bank-service/src/main/java/org/example/service/persistence/AccountEntityMapper.java   [NEW]

package org.example.service.persistence;

import org.example.model.*;
import org.example.service.AccountTypeFactory;
import org.springframework.stereotype.Component;

/**
 * Maps between the domain BankAccount and the JPA AccountEntity.
 * Keeps the domain pure and the entity persistence-focused.
 */
@Component
public class AccountEntityMapper {

    // domain → entity (for saving)
    public AccountEntity toEntity(BankAccount account) {
        return new AccountEntity(
                account.getAccountNumber(),
                account.getOwnerName(),
                account.getEmail(),
                account.getBalance(),
                account.getAccountType().displayName(),        // sealed → String
                account.getStatus(),                    // your real enum
                account.getCreatedAt()
        );
    }

    // entity → domain (for loading)
    public BankAccount toDomain(AccountEntity entity) {
        // rebuild the sealed AccountType from the stored String
        AccountType accountType = AccountTypeFactory.create(entity.getAccountType());

        return BankAccount.rehydrate(
                entity.getAccountNumber(),
                entity.getOwnerName(),
                entity.getEmail(),
                entity.getBalance(),
                accountType,
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}