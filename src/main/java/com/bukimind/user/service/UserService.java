package com.bukimind.user.service;

import com.bukimind.user.dto.UserDTO;
import com.bukimind.user.dto.UserPageResponse;
import com.bukimind.user.dto.UserRequestDTO;
import com.bukimind.user.entity.User;
import com.bukimind.common.exception.ResourceNotFoundException;
import com.bukimind.user.form.UserFilterForm;
import com.bukimind.user.mapper.UserMapper;
import com.bukimind.user.repository.IUserRepository;
import com.bukimind.user.specification.UserSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * Admin side of the application users.
 * Roles are Keycloak roles and are intentionally not managed here.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService{

    private final IUserRepository userRepository;

    private final UserMapper userMapper;

    // Get users with pagination and filter
    @Override
    public UserPageResponse getAllUsers(UserFilterForm form, Pageable pageable) {
        Specification<User> where = UserSpecification.buildWhere(form);
        Page<User> users = userRepository.findAll(where,pageable);
        Page<UserDTO> userDTOS = users.map(userMapper::toDTO);

        return new UserPageResponse(
                userDTOS.getContent(),
                userDTOS.getNumber(),
                userDTOS.getTotalElements(),
                userDTOS.getTotalPages(),
                userDTOS.getSize(),
                userDTOS.isLast(),
                userDTOS.isFirst()
        );
    }

    // Get a user by ID
    @Override
    public UserDTO getUserById(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        return userMapper.toDTO(user);
    }

    // Update the application profile of an existing user
    @Override
    public UserDTO updateUser(int id, UserRequestDTO userRequestDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with id: "+id));

        userMapper.updateEntityFromDTO(userRequestDTO, existingUser);
        User updatedUser = userRepository.save(existingUser);

        return userMapper.toDTO(updatedUser);
    }

    // Deletes user by ID
    @Override
    public void deleteUser(int id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: "+id);
        }
        userRepository.deleteById(id);
    }

}

