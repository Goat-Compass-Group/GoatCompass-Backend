/**
 * Value object dùng chung giữa các aggregate của taxonomy:
 * <ul>
 *   <li>{@link com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode}: mã ổn định, kiểm tra định dạng
 *       {@code meta-ads}.</li>
 *   <li>{@link com.goatcompass.taxonomy.domain.model.valueobject.ProficiencyLevel}: mức độ 1–4.</li>
 *   <li>{@link com.goatcompass.taxonomy.domain.model.valueobject.Alias}: một cách gọi của kỹ năng/nghề.</li>
 *   <li>{@link com.goatcompass.taxonomy.domain.model.valueobject.SkillRelation}: quan hệ tới kỹ năng khác.</li>
 * </ul>
 *
 * <p>Quy tắc: bất biến (dùng {@code record}), không có định danh, hai value object bằng nhau khi mọi giá trị
 * bằng nhau, và tự từ chối giá trị không hợp lệ ngay trong constructor.
 */
package com.goatcompass.taxonomy.domain.model.valueobject;
