package org.smauel.permissions.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.smauel.permissions.dto.RoleDto;
import org.smauel.permissions.dto.request.CreateRoleRequest;
import org.smauel.permissions.model.Role;

@Mapper(
        componentModel = "spring",
        uses = {PermissionMapper.class},
        unmappedTargetPolicy = ReportingPolicy.WARN)
public interface RoleMapper {
    RoleDto toDto(Role role);

    @Mapping(target = "updatedAt", ignore = true)
    Role toEntity(RoleDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Role toEntity(CreateRoleRequest request);
}
