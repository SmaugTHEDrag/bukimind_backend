package com.bukimind.common.security;

import com.bukimind.user.entity.User;
import com.bukimind.user.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * The single place that turns the authenticated Keycloak token into an application user:
 *
 *   JWT "sub"  ->  users.keycloak_user_id  ->  User
 *
 * The row is created on the first authenticated request and is never written again
 * afterwards, so an authenticated request does not trigger database writes.
 * The unique constraint on users.keycloak_user_id keeps the table consistent if the
 * very first requests of a new user arrive in parallel.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserService {

    private static final String CLAIM_PREFERRED_USERNAME = "preferred_username";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_NAME = "name";

    private final IUserRepository userRepository;

    public User getCurrentUser() {
        Jwt jwt = currentToken();

        return userRepository.findByKeycloakUserId(jwt.getSubject())
                .orElseGet(() -> userRepository.save(newUserFrom(jwt)));
    }

    private Jwt currentToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("No Keycloak token in the security context");
        }
        return jwt;
    }

    // first authenticated request of a Keycloak user: create the matching application user
    private User newUserFrom(Jwt jwt) {
        String username = jwt.getClaimAsString(CLAIM_PREFERRED_USERNAME);
        String email = jwt.getClaimAsString(CLAIM_EMAIL);

        User user = new User();
        user.setKeycloakUserId(jwt.getSubject());
        user.setUsername(username != null ? username : jwt.getSubject());
        user.setEmail(email != null ? email : jwt.getSubject() + "@keycloak.local");
        user.setFullName(jwt.getClaimAsString(CLAIM_NAME));
        user.setStatus(User.STATUS_ACTIVE);
        user.setLastLoginAt(LocalDateTime.now());
        return user;
    }
}
