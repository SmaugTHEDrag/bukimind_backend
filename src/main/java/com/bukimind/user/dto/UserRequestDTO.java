package com.bukimind.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Application profile data an admin may change.
 * Identity (keycloak_user_id), username and password live in Keycloak.
 */
@Data
public class UserRequestDTO {

    @NotBlank(message = "Email does not blank")
    @Email(message = "Email is valid")
    private String email;

    @Size(max = 255, message = "Full name must not be longer than 255 characters")
    private String fullName;

    @Size(max = 500, message = "Avatar URL must not be longer than 500 characters")
    private String avatarUrl;

    @NotBlank(message = "Status cannot be blank")
    @Pattern(regexp = "ACTIVE|SUSPENDED", message = "Status must be either ACTIVE or SUSPENDED")
    private String status;
}

