package com.goatcompass.taxonomy.infrastructure.persistence.mapper;

import com.goatcompass.taxonomy.domain.model.aggregate.JobFamily;
import com.goatcompass.taxonomy.infrastructure.persistence.entity.JobFamilyEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = TaxonomyCodeMapper.class)
public interface JobFamilyEntityMapper {

    JobFamily toDomain(JobFamilyEntity entity);
}
