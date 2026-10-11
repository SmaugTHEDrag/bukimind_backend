package com.bukimind.user.service;

import com.bukimind.user.dto.UserDTO;
import com.bukimind.user.dto.UserPageResponse;
import com.bukimind.user.dto.UserRequestDTO;
import com.bukimind.user.form.UserFilterForm;
import org.springframework.data.domain.Pageable;

public interface IUserService  {

    // Get paginated user with filters
    UserPageResponse getAllUsers(UserFilterForm form, Pageable pageable);

    // Get a user by ID
    UserDTO getUserById(int id);

    // Update the application profile of an existing user
    UserDTO updateUser(int id, UserRequestDTO userRequestDTO);

    // Delete a user by ID
    void deleteUser(int id);
}

