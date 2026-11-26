package com.goatcompass.taxonomy.infrastructure.persistence.mapper;

import com.goatcompass.taxonomy.domain.model.valueobject.Alias;
import com.goatcompass.taxonomy.domain.model.aggregate.Skill;
import com.goatcompass.taxonomy.domain.model.valueobject.SkillRelation;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.embeddable.AliasEmbeddable;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.SkillEntity;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.SkillRelationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = TaxonomyCodeMapper.class)
public interface SkillEntityMapper {

    Skill toDomain(SkillEntity entity);

    @Mapping(target = "text", source = "alias")
    Alias toDomain(AliasEmbeddable alias);

    @Mapping(target = "target", source = "toSkill")
    SkillRelation toDomain(SkillRelationEntity entity);
}
