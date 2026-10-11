package com.bukimind.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** POST /api/auth/refresh request. */
public record RefreshRequest(
        @NotBlank(message = "Refresh token is required") String refreshToken) {
}