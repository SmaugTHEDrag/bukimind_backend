package com.bukimind.user.mapper;

import com.bukimind.user.dto.UserDTO;
import com.bukimind.user.dto.UserRequestDTO;
import com.bukimind.user.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDTO(User user);

    // json null means "leave unchanged", not "set to null"
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(UserRequestDTO userRequestDTO, @MappingTarget User existingUser);
}

