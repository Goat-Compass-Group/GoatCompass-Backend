package com.goatcompass.taxonomy.infrastructure.persistence.entity;

import com.goatcompass.taxonomy.domain.model.enumeration.CredentialRequirement;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
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
@Table(name = "occupation_credential")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OccupationCredentialEntity {
    @EmbeddedId
    private Id id;
    @Column(columnDefinition = "text")
    @Setter
    private String noteVi;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter
    private CredentialRequirement requirement;

    @MapsId("occupationId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "occupation_id", foreignKey = @ForeignKey(name = "fk_occupation_credential_occupation"))
    private OccupationEntity occupation;

    @MapsId("credentialId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credential_id", foreignKey = @ForeignKey(name = "fk_occupation_credential_credential"))
    private CredentialEntity credential;

    public OccupationCredentialEntity(
            OccupationEntity occupation, CredentialEntity credential,
            CredentialRequirement requirement, String noteVi
    ) {
        this.id = new Id(occupation.getId(), credential.getId());
        this.occupation = occupation;
        this.credential = credential;
        this.requirement = requirement;
        this.noteVi = noteVi;
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Id implements Serializable {
        private Long occupationId;
        private Long credentialId;
    }
}
