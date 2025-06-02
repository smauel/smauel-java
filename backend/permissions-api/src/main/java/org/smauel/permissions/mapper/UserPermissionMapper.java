package org.smauel.permissions.mapper;

import org.mapstruct.Mapper;
import org.smauel.permissions.dto.UserPermissionDto;
import org.smauel.permissions.model.UserPermission;

@Mapper(
        componentModel = "spring",
        uses = {PermissionMapper.class, RoleMapper.class})
public interface UserPermissionMapper {
    UserPermissionDto toDto(UserPermission userPermission);

    UserPermission toEntity(UserPermissionDto dto);
}
