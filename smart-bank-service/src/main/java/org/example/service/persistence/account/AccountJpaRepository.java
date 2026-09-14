package org.example.service.persistence.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, String> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByOwnerNameIgnoreCase(String ownerName);

    List<AccountEntity> findByOwnerNameContainingIgnoreCase(String searchTerm);
    @Query("SELECT DISTINCT a FROM AccountEntity a LEFT JOIN FETCH a.transactions")
    List<AccountEntity> findAllWithTransactions();
}