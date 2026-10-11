package com.bukimind.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** POST /api/auth/login request. */
public record LoginRequest(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Password is required") String password) {
}