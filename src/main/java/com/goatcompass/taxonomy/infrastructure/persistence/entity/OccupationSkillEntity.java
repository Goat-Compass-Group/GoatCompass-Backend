package com.goatcompass.taxonomy.infrastructure.persistence.entity;

import com.goatcompass.taxonomy.domain.model.enumeration.Importance;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "occupation_skill",
        indexes = @Index(name = "idx_occupation_skill_skill", columnList = "skill_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OccupationSkillEntity {
    @EmbeddedId
    private Id id;
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Importance importance;
    @Setter
    @Column(nullable = false)
    private short typicalLevel;

    @MapsId("occupationId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "occupation_id", foreignKey = @ForeignKey(name = "fk_occupation_skill_occupation"))
    private OccupationEntity occupation;

    @MapsId("skillId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", foreignKey = @ForeignKey(name = "fk_occupation_skill_skill"))
    private SkillEntity skill;

    public OccupationSkillEntity(OccupationEntity occupation, SkillEntity skill, Importance importance, short typicalLevel) {
        this.id = new Id(occupation.getId(), skill.getId());
        this.occupation = occupation;
        this.skill = skill;
        this.importance = importance;
        this.typicalLevel = typicalLevel;
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Id implements Serializable {
        private Long occupationId;
        private Long skillId;
    }
}
