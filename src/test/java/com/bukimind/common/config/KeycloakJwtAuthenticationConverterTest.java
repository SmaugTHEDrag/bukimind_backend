package com.bukimind.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Keycloak realm roles must become Spring Security ROLE_* authorities, and the
 * principal name must be the token subject (the stable application identity).
 */
class KeycloakJwtAuthenticationConverterTest {

    private final KeycloakJwtAuthenticationConverter converter = new KeycloakJwtAuthenticationConverter();

    private Jwt tokenWithRoles(String... roles) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("kc-user-1")
                .claim("realm_access", Map.of("roles", List.of(roles)))
                .build();
    }

    @Test
    void realmRolesBecomeRoleAuthorities() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) converter.convert(tokenWithRoles("ADMIN", "CUSTOMER"));

        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_CUSTOMER");
    }

    @Test
    void keycloakDefaultCompositeRoleIsIgnored() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) converter.convert(
                tokenWithRoles("default-roles-bukimind", "CUSTOMER"));

        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_CUSTOMER");
    }

    @Test
    void principalNameIsTheTokenSubject() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) converter.convert(tokenWithRoles("ADMIN"));

        assertThat(authentication.getName()).isEqualTo("kc-user-1");
    }

    @Test
    void tokenWithoutRealmRolesHasNoAuthorities() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("kc-user-1")
                .build();

        assertThat(converter.convert(jwt).getAuthorities()).isEmpty();
    }
}
