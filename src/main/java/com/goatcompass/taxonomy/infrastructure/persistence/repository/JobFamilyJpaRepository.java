package com.goatcompass.taxonomy.infrastructure.persistence.repository;

import com.goatcompass.taxonomy.infrastructure.persistence.entity.JobFamilyEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobFamilyJpaRepository extends JpaRepository<JobFamilyEntity, Long> {
    List<JobFamilyEntity> findAllByOrderBySortOrderAsc();
}
