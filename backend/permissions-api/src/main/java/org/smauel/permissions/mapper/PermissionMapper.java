package org.smauel.permissions.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.smauel.permissions.dto.PermissionDto;
import org.smauel.permissions.model.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionDto toDto(Permission permission);

    @Mapping(target = "updatedAt", ignore = true)
    Permission toEntity(PermissionDto dto);
}
