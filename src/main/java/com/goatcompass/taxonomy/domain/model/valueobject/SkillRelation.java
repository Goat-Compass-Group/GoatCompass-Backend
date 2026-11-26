package com.goatcompass.taxonomy.domain.model.valueobject;

import com.goatcompass.taxonomy.domain.model.enumeration.RelationType;

import java.math.BigDecimal;

/**
 * Value object: quan hệ đi ra từ một kỹ năng tới kỹ năng {@code target}.
 * Tham chiếu kỹ năng khác bằng mã (không giữ object), đúng nguyên tắc aggregate chỉ trỏ nhau bằng định danh.
 *
 * @param weight mức chuyển đổi (0, 1], chỉ có với RELATED
 */
public record SkillRelation(RelationType type, TaxonomyCode target, BigDecimal weight) {
}
