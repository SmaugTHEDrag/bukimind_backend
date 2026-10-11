package com.bukimind.auth.service;

/**
 * Thrown when Keycloak cannot be reached or answers with an unexpected error.
 * Answered with 502 - the internal Keycloak response is only logged, never
 * forwarded to the client.
 */
public class KeycloakIntegrationException extends RuntimeException {

    public KeycloakIntegrationException(String message) {
        super(message);
    }

    public KeycloakIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}