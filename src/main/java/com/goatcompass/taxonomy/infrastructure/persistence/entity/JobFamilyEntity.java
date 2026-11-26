package com.goatcompass.taxonomy.infrastructure.persistence.entity;

import com.goatcompass.taxonomy.domain.model.enumeration.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job_family",
        uniqueConstraints = @UniqueConstraint(name = "uk_job_family_code", columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobFamilyEntity extends CodedEntity {
    @Column(columnDefinition = "text")
    private String descriptionVi;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.DRAFT;
    @Column(nullable = false)
    private int sortOrder;

    public JobFamilyEntity(String code, String nameVi, String nameEn) {
        super(code, nameVi, nameEn);
    }
}
