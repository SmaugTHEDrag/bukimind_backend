package com.bukimind.user.mapper;

import com.bukimind.user.dto.UserDTO;
import com.bukimind.user.dto.UserRequestDTO;
import com.bukimind.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDTO(User user);

    // json null means "leave unchanged", not "set to null"
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakUserId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDTO(UserRequestDTO userRequestDTO, @MappingTarget User existingUser);
}

