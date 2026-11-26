/**
 * Giá trị nhúng ({@code @Embeddable}) của JPA: dữ liệu nằm ở bảng con nhưng KHÔNG phải entity, vì không có
 * định danh riêng và không tồn tại tách khỏi entity chứa nó. Được khai báo bằng {@code @ElementCollection}
 * trong entity cha; Hibernate tự ghi/xoá theo entity cha.
 *
 * <p>Tương ứng với value object trong {@code domain.model}: {@code AliasEmbeddable} ↔ {@code Alias},
 * {@code CredentialSkillEmbeddable} ↔ {@code Credential.EvidencedSkill}.
 */
package com.goatcompass.taxonomy.infrastructure.persistence.entity.embeddable;
