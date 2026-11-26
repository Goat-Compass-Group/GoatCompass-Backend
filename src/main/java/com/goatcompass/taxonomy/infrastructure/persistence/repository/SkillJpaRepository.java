package com.goatcompass.taxonomy.infrastructure.persistence.repository;

import com.goatcompass.taxonomy.infrastructure.persistence.entity.SkillEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SkillJpaRepository extends JpaRepository<SkillEntity, Long> {
    Optional<SkillEntity> findByCode(String code);

    @Query("select distinct s from SkillEntity s join s.aliases a where a.normalized = :normalized")
    List<SkillEntity> findByAliasNormalized(String normalized);
}
