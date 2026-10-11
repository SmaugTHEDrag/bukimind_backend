package com.bukimind.common.security;

import com.bukimind.user.entity.User;
import com.bukimind.user.repository.IUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The application user is resolved from the JWT "sub" (users.keycloak_user_id),
 * created on the first authenticated request and never written again.
 */
@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private Jwt keycloakToken() {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("kc-123")
                .claim("preferred_username", "alice")
                .claim("email", "alice@example.com")
                .claim("name", "Alice Doe")
                .build();
    }

    private void authenticate(Jwt jwt) {
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt, List.of(), jwt.getSubject()));
    }

    @Test
    void existingUserIsLoadedByKeycloakSubjectAndNotUpdated() {
        authenticate(keycloakToken());
        User existing = new User();
        existing.setId(7);
        existing.setKeycloakUserId("kc-123");
        when(userRepository.findByKeycloakUserId("kc-123")).thenReturn(Optional.of(existing));

        assertThat(currentUserService.getCurrentUser()).isSameAs(existing);
        verify(userRepository, never()).save(any());
    }

    @Test
    void unknownUserIsCreatedFromTheToken() {
        authenticate(keycloakToken());
        when(userRepository.findByKeycloakUserId("kc-123")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = currentUserService.getCurrentUser();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getKeycloakUserId()).isEqualTo("kc-123");
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
        assertThat(saved.getFullName()).isEqualTo("Alice Doe");
        assertThat(saved.getStatus()).isEqualTo(User.STATUS_ACTIVE);
        assertThat(saved.getLastLoginAt()).isNotNull();
        assertThat(created).isSameAs(saved);
    }

    @Test
    void missingTokenFailsLoudly() {
        assertThatThrownBy(() -> currentUserService.getCurrentUser())
                .isInstanceOf(IllegalStateException.class);
    }
}
