package com.bukimind.common.security;

import com.bukimind.user.entity.User;
import com.bukimind.user.repository.IUserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.OffsetDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component 
@RequiredArgsConstructor 
@Slf4j 
public class KeycloakUserSyncFilter extends OncePerRequestFilter{
    private final IUserRepository userRepository;

    @Override 
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws IOException, ServletException {
        // Implement your filter logic here
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            try {
                syncUser(jwt);
            } catch (Exception e) {
                log.error("Error syncing user from Keycloak JWT: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    public void syncUser(Jwt jwt) {
        String keycloakUserId = jwt.getSubject();
        if (keycloakUserId == null) return;

        String email = jwt.getClaimAsString("email");
        String username = jwt.getClaimAsString("preferred_username");
        String fullName = jwt.getClaimAsString("name");

        String resolvedUsername = username;
        if (resolvedUsername == null || resolvedUsername.isBlank()) {
            resolvedUsername = email != null ? email : keycloakUserId;
        }
        String resolvedEmail = email;
        if (resolvedEmail == null || resolvedEmail.isBlank()) {
            resolvedEmail = resolvedUsername + "@keycloak.local";
        }
        final String userEmail = resolvedEmail;
        final String userUsername = resolvedUsername;

        try {
            User user = userRepository.findByKeycloakUserId(keycloakUserId)
                    .orElseGet(() -> User.builder()
                            .keycloakUserId(keycloakUserId)
                            .email(userEmail)
                            .username(userUsername)
                            .fullName(fullName)
                            .status(User.STATUS_ACTIVE)
                            .createdAt(OffsetDateTime.now())
                            .build());

            user.setUpdatedAt(OffsetDateTime.now());
            if (fullName != null && !fullName.isBlank()) {
                user.setFullName(fullName);
            }

            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            log.warn("User {} has been initialized by a parallel request.", keycloakUserId);
        }
    }
}
