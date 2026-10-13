package com.bukimind.common.exception;

/**
 * Thrown when a username or email already exists in Keycloak.
 * Answered with 409 by AuthController.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}