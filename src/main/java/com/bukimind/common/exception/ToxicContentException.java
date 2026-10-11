package com.bukimind.common.exception;

import org.springframework.http.HttpStatus;

public class ToxicContentException extends RuntimeException {
    private final HttpStatus status;

    public ToxicContentException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
