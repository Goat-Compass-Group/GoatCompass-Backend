package com.goatcompass.taxonomy.infrastructure.persistence.repository.adapter;

import com.goatcompass.taxonomy.domain.model.aggregate.Occupation;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;
import com.goatcompass.taxonomy.domain.repository.OccupationRepository;
import com.goatcompass.taxonomy.infrastructure.persistence.mapper.OccupationEntityMapper;
import java.util.List;
import java.util.Optional;

import com.goatcompass.taxonomy.infrastructure.persistence.repository.OccupationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class OccupationRepositoryAdapter implements OccupationRepository {
    private final OccupationEntityMapper mapper;
    private final OccupationJpaRepository jpa;

    @Override
    public Optional<Occupation> findByCode(TaxonomyCode code) {
        return jpa.findByCode(code.value()).map(mapper::toDomain);
    }

    @Override
    public List<Occupation> findByNormalizedAlias(String normalizedAlias) {
        return jpa.findByAliasNormalized(normalizedAlias).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Occupation> findByJobFamily(TaxonomyCode jobFamily) {
        return jpa.findByJobFamilyCode(jobFamily.value()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Occupation> findBySkill(TaxonomyCode skill) {
        return jpa.findBySkillCode(skill.value()).stream().map(mapper::toDomain).toList();
    }
}