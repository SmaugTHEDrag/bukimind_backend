package com.bukimind.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Raw Keycloak token endpoint response (snake_case). Internal to the backend
 * only - KeycloakAuthService maps it to the camelCase TokenResponse.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") Long expiresIn,
        @JsonProperty("refresh_expires_in") Long refreshExpiresIn,
        @JsonProperty("token_type") String tokenType) {

    /** Public (camelCase) view handed to the frontends. */
    public TokenResponse toTokenResponse() {
        return new TokenResponse(accessToken, refreshToken, expiresIn, refreshExpiresIn, tokenType);
    }
}