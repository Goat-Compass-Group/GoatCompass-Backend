package com.goatcompass.taxonomy.infrastructure.persistence.mapper;

import com.goatcompass.taxonomy.domain.model.valueobject.ProficiencyLevel;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.CodedEntity;
import org.mapstruct.Mapper;

/**
 * Chuyển đổi dùng chung cho các mapper của taxonomy: mã chuỗi / entity → {@link TaxonomyCode}, số → mức độ.
 * Quan hệ giữa các aggregate được chuyển thành mã, không giữ object.
 */
@Mapper(componentModel = "spring")
public interface TaxonomyCodeMapper {

    default TaxonomyCode toCode(String code) {
        return code == null ? null : TaxonomyCode.of(code);
    }

    default TaxonomyCode toCode(CodedEntity entity) {
        return entity == null ? null : TaxonomyCode.of(entity.getCode());
    }

    default ProficiencyLevel toLevel(short level) {
        return ProficiencyLevel.of(level);
    }
}
