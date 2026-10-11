package com.bukimind.user.controller;

import com.bukimind.user.dto.UserDTO;
import com.bukimind.user.dto.UserPageResponse;
import com.bukimind.user.dto.UserRequestDTO;
import com.bukimind.user.form.UserFilterForm;
import com.bukimind.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Application user administration. Accounts are created by Keycloak on
 * registration; the row here is created on the user's first authenticated request.
 */
@RestController
@RequestMapping("api/users")
@Tag(name = "User API", description = "APIs for application users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @Operation(summary = "Get all users", description = "Admin can get paginated list of users")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<UserPageResponse> getAllUsers(UserFilterForm form, @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
        return ResponseEntity.ok(userService.getAllUsers(form, pageable));
    }

    @Operation(summary = "Get user by ID", description = "Only admin can get user by ID")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable int id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Update user profile", description = "Only admin can update a user profile")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable int id, @RequestBody @Valid UserRequestDTO userRequestDTO){
        UserDTO updateUserDTO = userService.updateUser(id, userRequestDTO);
        return ResponseEntity.ok(updateUserDTO);
    }

    @Operation(summary = "Delete user", description = "Only admin can delete user")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable int id){
        userService.deleteUser(id);
        return new ResponseEntity<>("Id: "+id+" is deleted", HttpStatus.OK);
    }
}

