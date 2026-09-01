package org.example.service.persistence.account;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, String> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByOwnerNameIgnoreCase(String ownerName);

    java.util.List<AccountEntity> findByOwnerNameContainingIgnoreCase(String searchTerm);
}