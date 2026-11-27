package com.goatcompass.taxonomy.domain.repository;

import com.goatcompass.taxonomy.domain.model.aggregate.Occupation;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;
import java.util.List;
import java.util.Optional;

public interface OccupationRepository {
    Optional<Occupation> findByCode(TaxonomyCode code);

    List<Occupation> findByNormalizedAlias(String normalizedAlias);

    List<Occupation> findByJobFamily(TaxonomyCode jobFamily);

    List<Occupation> findBySkill(TaxonomyCode skill);
}