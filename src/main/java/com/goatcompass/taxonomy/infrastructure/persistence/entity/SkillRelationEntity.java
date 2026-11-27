package com.goatcompass.taxonomy.infrastructure.persistence.entity;

import com.goatcompass.taxonomy.domain.model.enumeration.RelationType;

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
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "skill_relation",
        indexes = @Index(name = "idx_skill_relation_to", columnList = "to_skill_id, relation_type"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkillRelationEntity {
    @EmbeddedId
    private Id id;
    /** Mức chuyển đổi (0, 1], chỉ dùng cho RELATED. */
    @Column(precision = 3, scale = 2)
    @Setter
    private BigDecimal weight;

    @MapsId("fromSkillId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_skill_id", foreignKey = @ForeignKey(name = "fk_skill_relation_from"))
    private SkillEntity fromSkill;

    @MapsId("toSkillId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_skill_id", foreignKey = @ForeignKey(name = "fk_skill_relation_to"))
    private SkillEntity toSkill;

    public SkillRelationEntity(SkillEntity fromSkill, SkillEntity toSkill, RelationType type, BigDecimal weight) {
        this.id = new Id(fromSkill.getId(), toSkill.getId(), type);
        this.fromSkill = fromSkill;
        this.toSkill = toSkill;
        this.weight = weight;
    }

    public RelationType getType() {
        return id.getRelationType();
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Id implements Serializable {
        private Long fromSkillId;
        private Long toSkillId;
        @Enumerated(EnumType.STRING)
        @Column(length = 20)
        private RelationType relationType;
    }
}
