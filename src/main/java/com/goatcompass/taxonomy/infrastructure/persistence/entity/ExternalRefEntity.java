package com.goatcompass.taxonomy.infrastructure.persistence.entity;

import com.goatcompass.taxonomy.domain.model.enumeration.ExternalScheme;
import com.goatcompass.taxonomy.domain.model.enumeration.MatchType;
import com.goatcompass.taxonomy.domain.model.enumeration.RefEntityType;

import com.goatcompass.shared.infrastructure.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "external_ref",
        uniqueConstraints = @UniqueConstraint(name = "uk_external_ref_entity_scheme_code",
                columnNames = {"entity_type", "entity_id", "scheme", "external_code"}),
        indexes = @Index(name = "idx_external_ref_lookup", columnList = "scheme, external_code"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ExternalRefEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefEntityType entityType;
    @Column(nullable = false)
    private Long entityId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExternalScheme scheme;
    @Column(nullable = false)
    private String externalCode;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchType matchType;

    public ExternalRefEntity(
            RefEntityType entityType, Long entityId, ExternalScheme scheme,
            String externalCode, MatchType matchType
    ) {
        this(null, entityType, entityId, scheme, externalCode, matchType);
    }
}
