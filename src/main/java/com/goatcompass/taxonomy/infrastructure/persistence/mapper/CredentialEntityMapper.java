package com.goatcompass.taxonomy.infrastructure.persistence.mapper;

import com.goatcompass.taxonomy.domain.model.aggregate.Credential;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.CredentialEntity;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.embeddable.CredentialSkillEmbeddable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = TaxonomyCodeMapper.class)
public interface CredentialEntityMapper {

    Credential toDomain(CredentialEntity entity);

    Credential.EvidencedSkill toDomain(CredentialSkillEmbeddable skill);
}
