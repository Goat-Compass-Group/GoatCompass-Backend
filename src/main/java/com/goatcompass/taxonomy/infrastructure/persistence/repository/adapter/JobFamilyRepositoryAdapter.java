package com.goatcompass.taxonomy.infrastructure.persistence.repository.adapter;

import com.goatcompass.taxonomy.domain.model.aggregate.JobFamily;
import com.goatcompass.taxonomy.domain.repository.JobFamilyRepository;
import com.goatcompass.taxonomy.infrastructure.persistence.mapper.JobFamilyEntityMapper;
import java.util.List;

import com.goatcompass.taxonomy.infrastructure.persistence.repository.JobFamilyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JobFamilyRepositoryAdapter implements JobFamilyRepository {
    private final JobFamilyEntityMapper mapper;
    private final JobFamilyJpaRepository jpa;

    @Override
    public List<JobFamily> findAll() {
        return jpa.findAllByOrderBySortOrderAsc().stream().map(mapper::toDomain).toList();
    }
}