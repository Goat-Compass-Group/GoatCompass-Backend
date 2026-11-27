package com.goatcompass.taxonomy.infrastructure.persistence.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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
@Table(name = "occupation_job_family",
        indexes = @Index(name = "idx_occupation_job_family_family", columnList = "job_family_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OccupationJobFamilyEntity {
    @EmbeddedId
    private Id id;
    @Column(name = "is_primary", nullable = false)
    @Setter
    private boolean primaryFamily;

    @MapsId("occupationId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "occupation_id", foreignKey = @ForeignKey(name = "fk_occupation_job_family_occupation"))
    private OccupationEntity occupation;

    @MapsId("jobFamilyId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_family_id", foreignKey = @ForeignKey(name = "fk_occupation_job_family_family"))
    private JobFamilyEntity jobFamily;

    public OccupationJobFamilyEntity(OccupationEntity occupation, JobFamilyEntity jobFamily, boolean primaryFamily) {
        this.id = new Id(occupation.getId(), jobFamily.getId());
        this.occupation = occupation;
        this.jobFamily = jobFamily;
        this.primaryFamily = primaryFamily;
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Id implements Serializable {
        private Long occupationId;
        private Long jobFamilyId;
    }
}
