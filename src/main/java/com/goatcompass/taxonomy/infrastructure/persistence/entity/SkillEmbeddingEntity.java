package com.goatcompass.taxonomy.infrastructure.persistence.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Vector của kỹ năng (tên + alias + mô tả) để tìm ứng viên gần đúng khi alias không khớp chính xác.
 * Khoá gồm cả model để chạy song song hai model khi chuyển đổi.
 * Index HNSW nằm trong changelog viết tay (JPA không mô tả được).
 */
@Entity
@Table(name = "skill_embedding")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkillEmbeddingEntity {
    public static final int DIMENSIONS = 1024;

    @EmbeddedId
    private Id id;
    @Column(nullable = false, length = 64)
    private String sourceHash;
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = DIMENSIONS)
    @Column(nullable = false)
    private float[] embedding;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @MapsId("skillId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", foreignKey = @ForeignKey(name = "fk_skill_embedding_skill"))
    private SkillEntity skill;

    public SkillEmbeddingEntity(SkillEntity skill, String model, String sourceHash, float[] embedding) {
        this.id = new Id(skill.getId(), model);
        this.skill = skill;
        this.sourceHash = sourceHash;
        this.embedding = embedding;
    }

    public String getModel() {
        return id.getModel();
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Id implements Serializable {
        private Long skillId;
        @Column(length = 100)
        private String model;
    }
}
