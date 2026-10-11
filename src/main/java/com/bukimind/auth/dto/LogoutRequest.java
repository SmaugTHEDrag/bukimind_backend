package com.bukimind.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** POST /api/auth/logout request. */
public record LogoutRequest(
        @NotBlank(message = "Refresh token is required") String refreshToken) {
}