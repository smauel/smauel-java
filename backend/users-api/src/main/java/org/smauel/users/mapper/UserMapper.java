package org.smauel.users.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.smauel.users.dto.UserDto;
import org.smauel.users.model.User;

/**
 * Mapper for the {@link User} entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserDto userDto);
}
