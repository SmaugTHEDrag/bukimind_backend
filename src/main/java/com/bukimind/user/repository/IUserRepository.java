package com.bukimind.user.repository;

import com.bukimind.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface IUserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    // find user by username (public profile lookups)
    Optional<User> findByUsername(String username);

    // find user by the Keycloak subject ("sub") stored in the token
    Optional<User> findByKeycloakUserId(String keycloakUserId);

    Optional<User> findByEmail(String email);

    boolean existsByKeycloakUserId(String keycloakUserId);
}
