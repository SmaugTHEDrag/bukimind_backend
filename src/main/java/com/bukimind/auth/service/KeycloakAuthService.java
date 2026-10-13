package com.bukimind.auth.service;

import com.bukimind.auth.dto.KeycloakTokenResponse;
import com.bukimind.auth.dto.RegisterRequest;
import com.bukimind.auth.dto.TokenResponse;
import com.bukimind.auth.config.KeycloakProperties;
import com.bukimind.common.exception.BadCredentialsException;
import com.bukimind.common.exception.KeycloakIntegrationException;
import com.bukimind.common.exception.UserAlreadyExistsException;
import com.bukimind.user.entity.User;
import com.bukimind.user.repository.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Backend-to-Keycloak intermediary. The frontends never talk to Keycloak
 * directly (no redirect flow); they only call /api/auth/*, and this service
 * forwards:
 *
 *   login    -> grant_type=password        on the token endpoint
 *   refresh  -> grant_type=refresh_token   on the token endpoint
 *   logout   -> refresh_token hint        on the logout endpoint
 *   register -> Admin API (service account) to create the user + CUSTOMER role
 *
 * Keycloak error bodies are logged but NEVER forwarded to clients - the
 * frontends only see the generic messages defined here.
 */
@Service
public class KeycloakAuthService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAuthService.class);

    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    private static final String CUSTOMER_ROLE = "CUSTOMER";

    private static final ParameterizedTypeReference<List<Map<String, Object>>> USER_LIST =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<List<Map<String, Object>>> ROLE_LIST =
            new ParameterizedTypeReference<>() {
            };

    private final KeycloakProperties properties;
    private final IUserRepository userRepository;
    private final RestClient restClient = RestClient.create();

    public KeycloakAuthService(KeycloakProperties properties, IUserRepository userRepository) {
        this.properties = properties;
        this.userRepository = userRepository;
    }

    /**
     * Resource Owner Password Credentials grant. Returns the Keycloak token
     * response as-is (access token, refresh token, expiry).
     */
    public TokenResponse login(String username, String password) {
        MultiValueMap<String, String> form = grantType(GRANT_TYPE_PASSWORD);
        form.add("client_id", properties.getClientId());
        form.add("username", username);
        form.add("password", password);
        if (properties.getClientSecret() != null && !properties.getClientSecret().isBlank()) {
            form.add("client_secret", properties.getClientSecret());
        }
        return tokenRequest(form).toTokenResponse();
    }

    /** Refresh an expired access token with a valid refresh token. */
    public TokenResponse refresh(String refreshToken) {
        MultiValueMap<String, String> form = grantType(GRANT_TYPE_REFRESH_TOKEN);
        form.add("client_id", properties.getClientId());
        form.add("refresh_token", refreshToken);
        if (properties.getClientSecret() != null && !properties.getClientSecret().isBlank()) {
            form.add("client_secret", properties.getClientSecret());
        }
        return tokenRequest(form).toTokenResponse();
    }

    /**
     * Revoke the refresh token at Keycloak (end the SSO session server-side).
     * Any failure is logged and swallowed - local logout must always succeed.
     */
    public void logout(String refreshToken) {
        MultiValueMap<String, String> form = grantType(GRANT_TYPE_REFRESH_TOKEN);
        form.add("client_id", properties.getClientId());
        form.add("refresh_token", refreshToken);

        try {
            restClient.post()
                    .uri(properties.getLogoutEndpoint())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            log.warn("Keycloak logout failed with status {}: {}",
                    ex.getStatusCode().value(), ex.getResponseBodyAsString());
        } catch (RuntimeException ex) {
            log.warn("Keycloak logout failed: {}", ex.getMessage());
        }
    }

    /**
     * Create a Keycloak user through the Admin API using the backend client's
     * service account, then always assign the CUSTOMER realm role - any role
     * sent by a client is ignored by design (the request has no role field).
     *
     * Throws UserAlreadyExistsException (409) when username or email is taken.
     */
    public void register(RegisterRequest request) {
        String adminToken = adminToken();

        // Fail fast with 409 before creating anything.
        if (userExists(adminToken, "username", request.username())
                || userExists(adminToken, "email", request.email())) {
            throw new UserAlreadyExistsException("Username or email already exists");
        }

        Map<String, Object> body = Map.of(
                "username", request.username(),
                "email", request.email(),
                "firstName", request.firstName(),
                "lastName", request.lastName(),
                "enabled", true,
                "emailVerified", false,
                "requiredActions", List.of("VERIFY_EMAIL"),
                // A regular (non-temporary) password the user can log in with.
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", request.password(),
                        "temporary", false)));

        ResponseEntity<Void> creationResponse;
        try {
            creationResponse = restClient.post()
                    .uri(properties.getAdminRealmsUrl() + "/{realm}/users", properties.getRealm())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            int status = ex.getStatusCode().value();
            if (status == 409) {
                throw new UserAlreadyExistsException("Username or email already exists");
            }
            // Never forward Keycloak's error details to the client.
            log.error("Keycloak user creation failed with status {}: {}",
                    status, ex.getResponseBodyAsString());
            throw new KeycloakIntegrationException("Registration failed - please try again later");
        } catch (RuntimeException ex) {
            log.error("Keycloak user creation failed: {}", ex.getMessage());
            throw new KeycloakIntegrationException("Registration failed - please try again later", ex);
        }

        String userId = createdUserId(creationResponse.getHeaders().getLocation());
        if (userId == null) {
            log.warn("Keycloak did not return a user Location for '{}'; looking up the created user",
                    request.username());
            userId = findUserId(adminToken, request.username());
        }
        if (userId == null) {
            log.error("Created user '{}' could not be resolved in Keycloak", request.username());
            throw new KeycloakIntegrationException("Registration completed in Keycloak but could not be finalized");
        }

        userRepository.save(User.builder()
                .keycloakUserId(userId)
                .username(request.username())
                .email(request.email())
                .fullName(request.firstName() + " " + request.lastName())
                .status(User.STATUS_ACTIVE)
                .build());

        // These follow-up actions are best effort; failures are logged by their respective methods.
        assignCustomerRole(adminToken, userId, request.username());
        sendVerificationEmail(adminToken, userId);
    }

    private String createdUserId(URI location) {
        if (location == null || location.getPath() == null) {
            return null;
        }

        String path = location.getPath();
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash < 0 || lastSlash == path.length() - 1) {
            return null;
        }
        return path.substring(lastSlash + 1);
    }

    /** Service-account token (client_credentials) for the backend client. */
    private String adminToken() {
        MultiValueMap<String, String> form = grantType("client_credentials");
        form.add("client_id", properties.getBackendClientId());
        form.add("client_secret", properties.getBackendClientSecret());

        try {
            KeycloakTokenResponse token = tokenRequest(form);
            if (token.accessToken() == null || token.accessToken().isBlank()) {
                throw new KeycloakIntegrationException("Registration is temporarily unavailable");
            }
            return token.accessToken();
        } catch (KeycloakIntegrationException ex) {
            throw ex;
        } catch (BadCredentialsException ex) {
            log.error("Service account rejected - check backend client roles: {}", ex.getMessage());
            throw new KeycloakIntegrationException("Registration is temporarily unavailable");
        }
    }

    /** GET /admin/realms/{realm}/users?{param}={value}&exact=true */
    private boolean userExists(String adminToken, String param, String value) {
        try {
            List<Map<String, Object>> users = restClient.get()
                    .uri(properties.getAdminRealmsUrl()
                            + "/{realm}/users?{param}={value}&exact=true",
                            Map.of("realm", properties.getRealm(), "param", param, "value", value))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .retrieve()
                    .body(USER_LIST);
            return users != null && !users.isEmpty();
        } catch (RuntimeException ex) {
            // If the check itself fails (e.g. missing view-users), proceed and
            // let Keycloak's own 409 decide - logged for diagnosis.
            log.warn("Keycloak user lookup for {}={} failed: {}", param, value, ex.getMessage());
            return false;
        }
    }

    private String findUserId(String adminToken, String username) {
        try {
            List<Map<String, Object>> users = restClient.get()
                    .uri(properties.getAdminRealmsUrl()
                            + "/{realm}/users?username={username}&exact=true",
                            Map.of("realm", properties.getRealm(), "username", username))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .retrieve()
                    .body(USER_LIST);

            String userId = (users == null || users.isEmpty())
                    ? null
                    : String.valueOf(users.get(0).get("id"));
            if (userId == null || "null".equals(userId)) {
                return null;
            }
            return userId;
        } catch (RuntimeException ex) {
            log.error("Failed to find created Keycloak user '{}': {}", username, ex.getMessage());
            return null;
        }
    }

    /**
     * Assign the CUSTOMER realm role to the freshly created user.
     * Role lookup failures are logged; the endpoint still succeeds because the
     * account itself was created.
     */
    private void assignCustomerRole(String adminToken, String userId, String username) {
        try {
            List<Map<String, Object>> realmRoles = restClient.get()
                    .uri(properties.getAdminRealmsUrl() + "/{realm}/roles", properties.getRealm())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .retrieve()
                    .body(ROLE_LIST);

            Map<String, Object> customerRole = (realmRoles == null ? List.<Map<String, Object>>of() : realmRoles)
                    .stream()
                    .filter(role -> CUSTOMER_ROLE.equals(role.get("name")))
                    .findFirst()
                    .orElse(null);

            if (customerRole == null) {
                log.error("Realm role '{}' does not exist in realm '{}' - create it in Keycloak",
                        CUSTOMER_ROLE, properties.getRealm());
                return;
            }

            restClient.post()
                    .uri(properties.getAdminRealmsUrl() + "/{realm}/users/{id}/role-mappings/realm",
                            properties.getRealm(), userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(customerRole))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException ex) {
            log.error("Failed to assign {} role to '{}': {}", CUSTOMER_ROLE, username, ex.getMessage());
        }
    }

    /** Send a verification email through the Keycloak Admin API. */
    private void sendVerificationEmail(String adminToken, String userId) {
        try {
            restClient.put()
                    .uri(properties.getAdminRealmsUrl()
                                    + "/{realm}/users/{userId}/send-verify-email",
                            properties.getRealm(), userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException ex) {
            log.error("Failed to send Keycloak verification email for user '{}': {}", userId, ex.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Keycloak HTTP plumbing
    // ------------------------------------------------------------------

    /** Raw Keycloak response (snake_case) - mapped by the public methods. */
    private KeycloakTokenResponse tokenRequest(MultiValueMap<String, String> form) {
        try {
            return restClient.post()
                    .uri(properties.getTokenEndpoint())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KeycloakTokenResponse.class);
        } catch (RestClientResponseException ex) {
            int status = ex.getStatusCode().value();
            // 400/401 = bad credentials or invalid refresh token; 403 = direct
            // access grants disabled. Never forward Keycloak's error body.
            if (status == 400 || status == 401 || status == 403) {
                throw new BadCredentialsException("Invalid username or password");
            }
            log.error("Keycloak token request failed with status {}: {}",
                    status, ex.getResponseBodyAsString());
            throw new KeycloakIntegrationException("Authentication is temporarily unavailable");
        } catch (RuntimeException ex) {
            log.error("Keycloak token request failed: {}", ex.getMessage());
            throw new KeycloakIntegrationException("Authentication is temporarily unavailable", ex);
        }
    }

    private static MultiValueMap<String, String> grantType(String grantType) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", grantType);
        return form;
    }
}
