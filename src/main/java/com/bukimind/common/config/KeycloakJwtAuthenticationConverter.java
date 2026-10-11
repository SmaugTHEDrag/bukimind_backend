package com.bukimind.common.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps a Keycloak access token to a Spring Security authentication.
 *
 * - authorities come from realm_access.roles and are prefixed with ROLE_,
 *   so Keycloak role "ADMIN" is checked with hasRole('ADMIN') / ROLE_ADMIN
 * - the principal name is the token subject ("sub"), which is the stable
 *   Keycloak user id the application stores as users.keycloak_user_id
 */
@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    // Keycloak's auto-generated composite role, not an application role
    private static final String DEFAULT_ROLE_PREFIX = "default-roles-";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, extractRealmRoles(jwt), jwt.getSubject());
    }

    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS_CLAIM);
        if (realmAccess == null || !(realmAccess.get(ROLES_CLAIM) instanceof Collection<?> roles)) {
            return List.of();
        }

        return roles.stream()
                .map(String::valueOf)
                .filter(role -> !role.startsWith(DEFAULT_ROLE_PREFIX))
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase(Locale.ROOT)))
                .collect(Collectors.toSet());
    }
}
