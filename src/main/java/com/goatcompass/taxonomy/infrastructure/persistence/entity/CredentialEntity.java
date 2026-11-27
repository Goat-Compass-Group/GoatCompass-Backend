package com.goatcompass.taxonomy.infrastructure.persistence.entity;

import com.goatcompass.taxonomy.domain.model.enumeration.Status;
import com.goatcompass.taxonomy.domain.model.enumeration.CredentialKind;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.embeddable.CredentialSkillEmbeddable;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "credential",
        uniqueConstraints = @UniqueConstraint(name = "uk_credential_code", columnNames = "code"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CredentialEntity extends CodedEntity {
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CredentialKind kind;
    @Setter
    private String issuer;
    @Setter
    @Column(length = 500)
    private String url;
    @Setter
    private Short prepWeeks;
    @Setter
    private Short validityMonths;
    @Setter
    @Column(columnDefinition = "text")
    private String noteVi;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_id", foreignKey = @ForeignKey(name = "fk_credential_replaced_by"))
    private CredentialEntity replacedBy;

    @ElementCollection
    @CollectionTable
            (name = "credential_skill",
            joinColumns = @JoinColumn(name = "credential_id"),
            foreignKey = @ForeignKey(name = "fk_credential_skill_credential"))
    private final List<CredentialSkillEmbeddable> skills = new ArrayList<>();

    public CredentialEntity(String code, String nameVi, String nameEn, CredentialKind kind) {
        super(code, nameVi, nameEn);
        this.kind = kind;
    }

    public void deprecate(CredentialEntity replacement) {
        this.status = Status.DEPRECATED;
        this.replacedBy = replacement;
    }

    public void updateStatus(Status status, CredentialEntity replacement) {
        this.status = status;
        this.replacedBy = status == Status.DEPRECATED ? replacement : null;
    }
}
