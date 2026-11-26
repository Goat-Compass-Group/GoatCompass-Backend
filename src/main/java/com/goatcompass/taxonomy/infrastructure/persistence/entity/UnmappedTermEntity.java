package com.goatcompass.taxonomy.infrastructure.persistence.entity;

import com.goatcompass.taxonomy.domain.model.enumeration.UnmappedStatus;

import com.goatcompass.shared.infrastructure.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Cụm từ kỹ năng chưa ánh xạ được, chờ người quản trị duyệt (thêm alias / tạo kỹ năng / bỏ qua).
 * Chỉ lưu cụm từ, không lưu nội dung CV.
 */
@Entity
@Table(name = "unmapped_term",
        uniqueConstraints = @UniqueConstraint(name = "uk_unmapped_term_normalized", columnNames = "normalized"),
        indexes = @Index(name = "idx_unmapped_term_queue", columnList = "status, occurrences"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UnmappedTermEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String normalized;
    @Column(nullable = false)
    private String sampleText;
    @Column(length = CodedEntity.CODE_LENGTH)
    private String jobFamilyCode;
    @Column(nullable = false)
    private int occurrences = 1;
    @Column(nullable = false)
    private Instant firstSeenAt;
    @Column(nullable = false)
    private Instant lastSeenAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnmappedStatus status = UnmappedStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_skill_id", foreignKey = @ForeignKey(name = "fk_unmapped_term_resolved_skill"))
    private SkillEntity resolvedSkill;

    public UnmappedTermEntity(String normalized, String sampleText, String jobFamilyCode, Instant seenAt) {
        this.normalized = normalized;
        this.sampleText = sampleText;
        this.jobFamilyCode = jobFamilyCode;
        this.firstSeenAt = seenAt;
        this.lastSeenAt = seenAt;
    }

    public void seenAgain(Instant at) {
        this.occurrences++;
        this.lastSeenAt = at;
    }

    public void resolve(UnmappedStatus status, SkillEntity skill) {
        this.status = status;
        this.resolvedSkill = skill;
    }
}
