package com.goatcompass.taxonomy.infrastructure.persistence.repository.adapter;

import com.goatcompass.taxonomy.domain.model.aggregate.Credential;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;
import com.goatcompass.taxonomy.domain.repository.CredentialRepository;
import com.goatcompass.taxonomy.infrastructure.persistence.mapper.CredentialEntityMapper;
import java.util.List;
import java.util.Optional;

import com.goatcompass.taxonomy.infrastructure.persistence.repository.CredentialJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class CredentialRepositoryAdapter implements CredentialRepository {
    private final CredentialEntityMapper mapper;
    private final CredentialJpaRepository jpa;

    @Override
    public Optional<Credential> findByCode(TaxonomyCode code) {
        return jpa.findByCode(code.value()).map(mapper::toDomain);
    }

    @Override
    public List<Credential> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }
}