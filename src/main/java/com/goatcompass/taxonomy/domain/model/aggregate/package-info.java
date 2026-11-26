/**
 * Aggregate root của taxonomy: {@link com.goatcompass.taxonomy.domain.model.aggregate.Skill},
 * {@link com.goatcompass.taxonomy.domain.model.aggregate.Occupation},
 * {@link com.goatcompass.taxonomy.domain.model.aggregate.Credential},
 * {@link com.goatcompass.taxonomy.domain.model.aggregate.JobFamily}.
 *
 * <p>Quy tắc:
 * <ul>
 *   <li>Định danh là mã ổn định ({@code TaxonomyCode}), không phải id của DB.</li>
 *   <li>Aggregate trỏ tới aggregate khác bằng mã, không giữ object
 *       (ví dụ {@code Occupation} giữ mã kỹ năng, không giữ {@code Skill}).</li>
 *   <li>Bất biến nghiệp vụ được kiểm tra ngay khi tạo (ví dụ kỹ năng QUICK phải học được trong 4 tuần)
 *       và thay đổi trạng thái qua phương thức có tên nghiệp vụ ({@code deprecate}), không qua setter.</li>
 *   <li>Mỗi aggregate có một port repository tương ứng trong {@code domain.repository}.</li>
 *   <li>Value object chỉ thuộc riêng một aggregate được khai báo lồng bên trong aggregate đó
 *       (ví dụ {@code Occupation.SkillRequirement}, {@code Credential.EvidencedSkill}).</li>
 * </ul>
 */
package com.goatcompass.taxonomy.domain.model.aggregate;
