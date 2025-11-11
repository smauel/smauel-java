package org.smauel.users.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.smauel.users.dto.UserDto;
import org.smauel.users.dto.request.CreateUserRequest;
import org.smauel.users.model.User;

/**
 * Mapper for the {@link User} entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserDto userDto);

    @Mapping(target = "id", ignore = true)
    User toEntity(CreateUserRequest request);
}
