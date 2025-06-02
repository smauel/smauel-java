package org.smauel.permissions.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.smauel.permissions.dto.RoleDto;
import org.smauel.permissions.model.Role;

@Mapper(
        componentModel = "spring",
        uses = {PermissionMapper.class})
public interface RoleMapper {
    RoleDto toDto(Role role);

    @Mapping(target = "updatedAt", ignore = true)
    Role toEntity(RoleDto dto);
}
