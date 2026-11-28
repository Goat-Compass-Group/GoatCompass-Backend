package com.goatcompass.identity.infrastructure.persistence.repository;

import com.goatcompass.identity.infrastructure.persistence.entity.UserAccountEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, UUID> {
    Optional<UserAccountEntity> findByEmail(String email);

    Optional<UserAccountEntity> findFirstByUsernameIgnoreCaseOrEmail(String username, String email);

    boolean existsByUsernameIgnoreCaseOrEmail(String username, String email);
}
