package com.bukimind.user.repository;

import com.bukimind.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {

    // find user by username (public profile lookups)
    Optional<User> findByUsername(String username);

    // find user by the Keycloak subject ("sub") stored in the token
    Optional<User> findByKeycloakUserId(String keycloakUserId);
}

