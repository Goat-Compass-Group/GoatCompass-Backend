package com.goatcompass.taxonomy.infrastructure.persistence.repository;

import com.goatcompass.taxonomy.domain.model.enumeration.RefEntityType;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.ExternalRefEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalRefJpaRepository extends JpaRepository<ExternalRefEntity, Long> {

    List<ExternalRefEntity> findByEntityTypeAndEntityId(RefEntityType entityType, Long entityId);
}
