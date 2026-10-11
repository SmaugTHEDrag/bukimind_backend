package com.bukimind.auth.dto;

/**
 * The token payload the frontends receive from /api/auth/login and
 * /api/auth/refresh - camelCase, exactly as documented:
 * {accessToken, refreshToken, expiresIn, ...}. Keycloak's snake_case
 * (access_token, refresh_token) is mapped in KeycloakAuthService.
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn,
        Long refreshExpiresIn,
        String tokenType) {
}