package com.bukimind.auth.dto;

/**
 * Error body returned by the auth endpoints. Mirrors the GlobalExceptionHandler
 * shape ({"message": ...}) and never contains Keycloak internal error details.
 */
public record AuthError(String message) {

    public static AuthError of(String message) {
        return new AuthError(message);
    }
}