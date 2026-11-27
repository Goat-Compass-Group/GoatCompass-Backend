package com.goatcompass.taxonomy.domain.repository;

import com.goatcompass.taxonomy.domain.model.aggregate.Skill;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;
import java.util.List;
import java.util.Optional;

public interface SkillRepository {
    Optional<Skill> findByCode(TaxonomyCode code);

    List<Skill> findByNormalizedAlias(String normalizedAlias);

    List<Skill> findAll();
}