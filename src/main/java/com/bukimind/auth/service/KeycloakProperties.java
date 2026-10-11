package com.bukimind.auth.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Keycloak connection settings, read from environment-driven properties only
 * (never hardcoded). See application.properties for the property names.
 */
@Getter
@Component
public class KeycloakProperties {

    private final String baseUrl;      // e.g. http://localhost:8180
    private final String realm;        // e.g. bukimind
    private final String clientId;     // client used for password grant (frontend client)
    private final String clientSecret; // optional - only if the client is confidential
    private final String backendClientId;     // service-account client used for the Admin API
    private final String backendClientSecret;

    public KeycloakProperties(
            @Value("${app.keycloak.base-url}") String baseUrl,
            @Value("${app.keycloak.realm}") String realm,
            @Value("${app.keycloak.client-id}") String clientId,
            @Value("${app.keycloak.client-secret:}") String clientSecret,
            @Value("${app.keycloak.backend-client-id}") String backendClientId,
            @Value("${app.keycloak.backend-client-secret:}") String backendClientSecret) {
        this.baseUrl = baseUrl;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.backendClientId = backendClientId;
        this.backendClientSecret = backendClientSecret;
    }

    /** Token endpoint, e.g. http://localhost:8180/realms/bukimind/protocol/openid-connect/token */
    public String getTokenEndpoint() {
        return baseUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }

    /** Logout endpoint, e.g. http://localhost:8180/realms/bukimind/protocol/openid-connect/logout */
    public String getLogoutEndpoint() {
        return baseUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
    }

    /** Admin REST root, e.g. http://localhost:8180/admin/realms */
    public String getAdminRealmsUrl() {
        return baseUrl + "/admin/realms";
    }
}