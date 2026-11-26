/**
 * Adapter (outbound) của hexagonal architecture: hiện thực các port trong {@code taxonomy.domain.repository}
 * bằng Spring Data JPA.
 *
 * <ul>
 *   <li>{@link com.goatcompass.taxonomy.infrastructure.persistence.repository.adapter.SkillRepositoryAdapter}
 *       → {@code SkillRepository}</li>
 *   <li>{@link com.goatcompass.taxonomy.infrastructure.persistence.repository.adapter.OccupationRepositoryAdapter}
 *       → {@code OccupationRepository}</li>
 *   <li>{@link com.goatcompass.taxonomy.infrastructure.persistence.repository.adapter.CredentialRepositoryAdapter}
 *       → {@code CredentialRepository}</li>
 *   <li>{@link com.goatcompass.taxonomy.infrastructure.persistence.repository.adapter.JobFamilyRepositoryAdapter}
 *       → {@code JobFamilyRepository}</li>
 * </ul>
 *
 * <p>Mỗi adapter làm đúng ba việc: nhận tham số domain (ví dụ {@code TaxonomyCode}), gọi Spring Data repository
 * ở package cha, rồi chuyển entity sang domain model bằng mapper MapStruct ({@code *EntityMapper}).
 * Adapter không chứa nghiệp vụ; tầng application chỉ biết tới port, không biết adapter hay JPA tồn tại.
 *
 * <p>Adapter để phạm vi package (không {@code public}): không ai được gọi trực tiếp, Spring tự inject vào
 * nơi cần port tương ứng.
 */
package com.goatcompass.taxonomy.infrastructure.persistence.repository.adapter;
