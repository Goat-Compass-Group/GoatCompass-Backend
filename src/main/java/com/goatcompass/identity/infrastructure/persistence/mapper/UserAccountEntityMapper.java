package com.goatcompass.identity.infrastructure.persistence.mapper;

import com.goatcompass.identity.domain.model.aggregate.UserAccount;
import com.goatcompass.identity.infrastructure.persistence.entity.UserAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserAccountEntityMapper {

    UserAccount toDomain(UserAccountEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(UserAccount user, @MappingTarget UserAccountEntity entity);
}
