package com.goatcompass.taxonomy.infrastructure.persistence.entity;

import com.goatcompass.taxonomy.domain.model.enumeration.Status;

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

@Entity
@Table(name = "occupation",
        uniqueConstraints = @UniqueConstraint(name = "uk_occupation_code", columnNames = "code"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OccupationEntity extends CodedEntity {
    @Setter
    @Column(columnDefinition = "text")
    private String descriptionVi;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @ElementCollection
    @CollectionTable(
            name = "occupation_alias",
            joinColumns = @JoinColumn(name = "occupation_id"),
            foreignKey = @ForeignKey(name = "fk_occupation_alias_occupation"),
            indexes = @Index(name = "idx_occupation_alias_normalized", columnList = "normalized"))
    private final List<AliasEmbeddable> aliases = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_id", foreignKey = @ForeignKey(name = "fk_occupation_replaced_by"))
    private OccupationEntity replacedBy;

    @OneToMany(mappedBy = "occupation", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OccupationJobFamilyEntity> jobFamilies = new ArrayList<>();

    @OneToMany(mappedBy = "occupation", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OccupationSkillEntity> skills = new ArrayList<>();

    @OneToMany(mappedBy = "occupation", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OccupationCredentialEntity> credentials = new ArrayList<>();

    public OccupationEntity(String code, String nameVi, String nameEn) {
        super(code, nameVi, nameEn);
    }

    public void deprecate(OccupationEntity replacement) {
        this.status = Status.DEPRECATED;
        this.replacedBy = replacement;
    }

    public void updateStatus(Status status, OccupationEntity replacement) {
        this.status = status;
        this.replacedBy = status == Status.DEPRECATED ? replacement : null;
    }
}
