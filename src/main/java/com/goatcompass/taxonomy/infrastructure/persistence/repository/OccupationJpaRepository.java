package com.goatcompass.taxonomy.infrastructure.persistence.repository;

import com.goatcompass.taxonomy.infrastructure.persistence.entity.OccupationEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OccupationJpaRepository extends JpaRepository<OccupationEntity, Long> {
    Optional<OccupationEntity> findByCode(String code);

    @Query("select distinct o from OccupationEntity o join o.aliases a where a.normalized = :normalized")
    List<OccupationEntity> findByAliasNormalized(String normalized);

    @Query("select distinct o from OccupationEntity o join o.jobFamilies f where f.jobFamily.code = :jobFamilyCode")
    List<OccupationEntity> findByJobFamilyCode(String jobFamilyCode);

    @Query("select distinct o from OccupationEntity o join o.skills s where s.skill.code = :skillCode")
    List<OccupationEntity> findBySkillCode(String skillCode);
}
