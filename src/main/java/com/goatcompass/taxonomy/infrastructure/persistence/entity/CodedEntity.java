package com.goatcompass.taxonomy.infrastructure.persistence.entity;

import com.goatcompass.shared.infrastructure.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class CodedEntity extends BaseEntity {
    public static final int CODE_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = CODE_LENGTH)
    private String code;
    @Setter
    @Column(nullable = false)
    private String nameVi;
    @Setter
    @Column(nullable = false)
    private String nameEn;

    protected CodedEntity(String code, String nameVi, String nameEn) {
        this.code = code;
        this.nameVi = nameVi;
        this.nameEn = nameEn;
    }
}
