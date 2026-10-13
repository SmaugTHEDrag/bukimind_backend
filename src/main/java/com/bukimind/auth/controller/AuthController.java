package com.bukimind.auth.controller;

import com.bukimind.auth.dto.AuthError;
import com.bukimind.auth.dto.LoginRequest;
import com.bukimind.auth.dto.LogoutRequest;
import com.bukimind.auth.dto.RefreshRequest;
import com.bukimind.auth.dto.RegisterRequest;
import com.bukimind.common.exception.BadCredentialsException;
import com.bukimind.auth.service.KeycloakAuthService;
import com.bukimind.common.exception.KeycloakIntegrationException;
import com.bukimind.common.exception.UserAlreadyExistsException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public authentication endpoints (permitAll). The backend proxies Keycloak,
 * so the frontends never redirect to Keycloak and never talk to it directly.
 */
@RestController
@RequestMapping("api/auth")
@Tag(name = "Auth API", description = "Login, refresh, logout and registration via the backend")
@RequiredArgsConstructor
public class AuthController {

    private final KeycloakAuthService keycloakAuthService;

    @Operation(summary = "Login with username and password",
            description = "Calls Keycloak's token endpoint (password grant) and returns the tokens")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(keycloakAuthService.login(request.username(), request.password()));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthError.of(ex.getMessage()));
        } catch (KeycloakIntegrationException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(AuthError.of(ex.getMessage()));
        }
    }

    @Operation(summary = "Refresh the access token", description = "Calls Keycloak's token endpoint (refresh_token grant)")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest request) {
        try {
            return ResponseEntity.ok(keycloakAuthService.refresh(request.refreshToken()));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthError.of("Session expired - please log in again"));
        } catch (KeycloakIntegrationException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(AuthError.of(ex.getMessage()));
        }
    }

    @Operation(summary = "Logout", description = "Revokes the refresh token at Keycloak; always answers 200")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody LogoutRequest request) {
        keycloakAuthService.logout(request.refreshToken());
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @Operation(summary = "Register a new customer account",
            description = "Creates the user in Keycloak through the Admin API and always assigns the CUSTOMER role")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            keycloakAuthService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Account created"));
        } catch (UserAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(AuthError.of(ex.getMessage()));
        } catch (KeycloakIntegrationException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(AuthError.of(ex.getMessage()));
        }
    }
}