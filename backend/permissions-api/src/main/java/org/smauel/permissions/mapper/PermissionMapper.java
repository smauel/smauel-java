package org.smauel.permissions.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.smauel.permissions.dto.PermissionDto;
import org.smauel.permissions.dto.request.CreatePermissionRequest;
import org.smauel.permissions.model.Permission;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface PermissionMapper {
    PermissionDto toDto(Permission permission);

    @Mapping(target = "updatedAt", ignore = true)
    Permission toEntity(PermissionDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Permission toEntity(CreatePermissionRequest request);
}
