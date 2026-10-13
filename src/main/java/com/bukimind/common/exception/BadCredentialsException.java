package com.bukimind.common.exception;

/**
 * Thrown when Keycloak rejects credentials. Answered with 401 and a generic
 * message so Keycloak's internal error details never reach the client.
 */
public class BadCredentialsException extends RuntimeException {

    public BadCredentialsException(String message) {
        super(message);
    }
}