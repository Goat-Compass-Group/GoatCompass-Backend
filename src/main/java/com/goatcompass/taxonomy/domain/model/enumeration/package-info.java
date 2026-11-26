/**
 * Enum nghiệp vụ của module taxonomy (Java thuần, thuộc tầng domain).
 *
 * <ul>
 *   <li>{@link com.goatcompass.taxonomy.domain.model.enumeration.Status}: vòng đời một mục taxonomy
 *       (DRAFT, ACTIVE, DEPRECATED). Mã không bao giờ bị xoá, lỗi thời thì DEPRECATED.</li>
 *   <li>{@link com.goatcompass.taxonomy.domain.model.enumeration.SkillKind}: loại kỹ năng
 *       (chuyên môn, công cụ, kiến thức, ngoại ngữ, kỹ năng mềm).</li>
 *   <li>{@link com.goatcompass.taxonomy.domain.model.enumeration.Acquisition}: cách bù kỹ năng
 *       (QUICK: tối đa 4 tuần tới mức "làm được việc"; EXTENDED: cần vài tháng trở lên).</li>
 *   <li>{@link com.goatcompass.taxonomy.domain.model.enumeration.RelationType}: quan hệ giữa hai kỹ năng
 *       (BROADER, RELATED, PREREQUISITE).</li>
 *   <li>{@link com.goatcompass.taxonomy.domain.model.enumeration.CredentialKind}: loại chứng chỉ
 *       (CERTIFICATE tự nguyện, DEGREE bằng cấp, LICENSE bắt buộc theo luật).</li>
 *   <li>{@link com.goatcompass.taxonomy.domain.model.enumeration.Importance}: mức cần thiết của kỹ năng
 *       với một nghề (ESSENTIAL, OPTIONAL).</li>
 *   <li>{@link com.goatcompass.taxonomy.domain.model.enumeration.CredentialRequirement}: mức cần thiết của
 *       chứng chỉ với một nghề (LEGALLY_REQUIRED, COMMON, PREFERRED).</li>
 *   <li>{@link com.goatcompass.taxonomy.domain.model.enumeration.ExternalScheme},
 *       {@link com.goatcompass.taxonomy.domain.model.enumeration.MatchType},
 *       {@link com.goatcompass.taxonomy.domain.model.enumeration.RefEntityType}: liên kết sang bộ chuẩn
 *       bên ngoài (ESCO, O*NET, ISCO-08, VSCO), độ khớp, và loại mục được liên kết.</li>
 *   <li>{@link com.goatcompass.taxonomy.domain.model.enumeration.UnmappedStatus}: trạng thái duyệt
 *       của cụm từ chưa ánh xạ được về kỹ năng.</li>
 * </ul>
 *
 * <p>Các enum được lưu xuống DB dạng chuỗi ({@code @Enumerated(EnumType.STRING)}) và có CHECK constraint
 * liệt kê giá trị hợp lệ trong {@code db/changelog/schema/init-taxonomy-schema.xml}.
 * <b>Thêm hoặc đổi tên một giá trị thì phải thêm changeset sửa CHECK tương ứng</b>, nếu không lệnh ghi sẽ lỗi.
 * Không đổi tên giá trị đã có dữ liệu.
 */
package com.goatcompass.taxonomy.domain.model.enumeration;