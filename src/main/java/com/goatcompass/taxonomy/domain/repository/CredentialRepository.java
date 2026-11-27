package com.goatcompass.taxonomy.domain.repository;

import com.goatcompass.taxonomy.domain.model.aggregate.Credential;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;
import java.util.List;
import java.util.Optional;

public interface CredentialRepository {
    Optional<Credential> findByCode(TaxonomyCode code);

    List<Credential> findAll();
}