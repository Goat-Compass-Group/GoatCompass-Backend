package com.goatcompass.taxonomy.infrastructure.persistence.entity;

import com.goatcompass.taxonomy.domain.model.enumeration.Status;
import com.goatcompass.taxonomy.domain.model.enumeration.SkillKind;
import com.goatcompass.taxonomy.domain.model.enumeration.Acquisition;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.embeddable.AliasEmbeddable;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Kỹ năng, kiến thức, công cụ, ngoại ngữ hoặc kỹ năng mềm. Dùng chung mọi lĩnh vực. */
@Entity
@Table(name = "skill",
        uniqueConstraints = @UniqueConstraint(name = "uk_skill_code", columnNames = "code"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkillEntity extends CodedEntity {
    @Setter
    @Column(columnDefinition = "text")
    private String descriptionVi;
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SkillKind kind;
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Acquisition acquisition;
    @Setter
    private Short weeksToWorking;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_id", foreignKey = @ForeignKey(name = "fk_skill_replaced_by"))
    private SkillEntity replacedBy;

    @OneToMany(mappedBy = "fromSkill", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<SkillRelationEntity> relations = new ArrayList<>();

    @ElementCollection
    @CollectionTable
            (name = "skill_alias",
            joinColumns = @JoinColumn(name = "skill_id"),
            foreignKey = @ForeignKey(name = "fk_skill_alias_skill"),
            indexes = @Index(name = "idx_skill_alias_normalized", columnList = "normalized"))
    private final List<AliasEmbeddable> aliases = new ArrayList<>();

    public SkillEntity(String code, String nameVi, String nameEn, SkillKind kind, Acquisition acquisition) {
        super(code, nameVi, nameEn);
        this.kind = kind;
        this.acquisition = acquisition;
    }

    public void deprecate(SkillEntity replacement) {
        this.status = Status.DEPRECATED;
        this.replacedBy = replacement;
    }

    public void updateStatus(Status status, SkillEntity replacement) {
        this.status = status;
        this.replacedBy = status == Status.DEPRECATED ? replacement : null;
    }
}
