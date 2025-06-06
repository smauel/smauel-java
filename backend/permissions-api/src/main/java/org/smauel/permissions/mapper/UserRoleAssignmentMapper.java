package org.smauel.permissions.mapper;

import org.mapstruct.Mapper;
import org.smauel.permissions.dto.UserRoleAssignmentDto;
import org.smauel.permissions.model.UserRoleAssignment;

@Mapper(
        componentModel = "spring",
        uses = {RoleMapper.class})
public interface UserRoleAssignmentMapper {
    UserRoleAssignmentDto toDto(UserRoleAssignment userRoleAssignment);

    UserRoleAssignment toEntity(UserRoleAssignmentDto dto);
}
