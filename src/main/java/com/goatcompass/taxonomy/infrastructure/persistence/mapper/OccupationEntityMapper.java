package com.goatcompass.taxonomy.infrastructure.persistence.mapper;

import com.goatcompass.taxonomy.domain.model.valueobject.Alias;
import com.goatcompass.taxonomy.domain.model.aggregate.Occupation;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.embeddable.AliasEmbeddable;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.OccupationCredentialEntity;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.OccupationEntity;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.OccupationJobFamilyEntity;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.OccupationSkillEntity;
import java.util.Comparator;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = TaxonomyCodeMapper.class)
public interface OccupationEntityMapper {

    Occupation toDomain(OccupationEntity entity);

    @Mapping(target = "text", source = "alias")
    Alias toDomain(AliasEmbeddable alias);

    @Mapping(target = "level", source = "typicalLevel")
    Occupation.SkillRequirement toDomain(OccupationSkillEntity entity);

    Occupation.CredentialRequirementLink toDomain(OccupationCredentialEntity entity);

    /** Lĩnh vực chính đứng đầu danh sách. */
    default List<TaxonomyCode> toJobFamilyCodes(List<OccupationJobFamilyEntity> families) {
        return families.stream()
                .sorted(Comparator.comparing(OccupationJobFamilyEntity::isPrimaryFamily).reversed())
                .map(f -> TaxonomyCode.of(f.getJobFamily().getCode()))
                .toList();
    }
}
