package com.goatcompass.taxonomy.infrastructure.persistence.repository;

import com.goatcompass.taxonomy.infrastructure.persistence.entity.CredentialEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialJpaRepository extends JpaRepository<CredentialEntity, Long> {
    Optional<CredentialEntity> findByCode(String code);
}
