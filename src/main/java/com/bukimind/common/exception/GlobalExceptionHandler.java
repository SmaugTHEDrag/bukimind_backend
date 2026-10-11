package com.bukimind.common.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single place that turns application exceptions into the API error shape
 * {"message": "..."} (plus "errors" for bean validation).
 *
 * Authentication/authorization failures raised before the controller are answered by
 * SecurityConfig (AuthenticationEntryPoint -> 401, AccessDeniedHandler -> 403).
 *
 * Note: there is deliberately no catch-all Exception handler. A catch-all would swallow
 * AccessDeniedException thrown by @PreAuthorize and turn a 403 into a 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ToxicContentException.class)
    public ResponseEntity<?> handleToxicContent(ToxicContentException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<?> handleBadRequest(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    // @Valid failures on request bodies
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Validation failed", "errors", errors));
    }
}

