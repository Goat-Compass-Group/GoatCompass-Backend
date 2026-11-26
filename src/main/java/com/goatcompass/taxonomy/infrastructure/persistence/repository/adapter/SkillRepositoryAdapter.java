package com.goatcompass.taxonomy.infrastructure.persistence.repository.adapter;

import com.goatcompass.taxonomy.domain.model.aggregate.Skill;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;
import com.goatcompass.taxonomy.domain.repository.SkillRepository;
import com.goatcompass.taxonomy.infrastructure.persistence.mapper.SkillEntityMapper;
import java.util.List;
import java.util.Optional;

import com.goatcompass.taxonomy.infrastructure.persistence.repository.SkillJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class SkillRepositoryAdapter implements SkillRepository {

    private final SkillEntityMapper mapper;
    private final SkillJpaRepository jpa;

    @Override
    public Optional<Skill> findByCode(TaxonomyCode code) {
        return jpa.findByCode(code.value()).map(mapper::toDomain);
    }

    @Override
    public List<Skill> findByNormalizedAlias(String normalizedAlias) {
        return jpa.findByAliasNormalized(normalizedAlias).stream().map(mapper::toDomain).toList();
    }
}