package com.bukimind.ai.chatbox.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatResponse {

    // response message from the chatbot
    private String content;

    // response creation time
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    // true if this response represents an error
    private boolean isError;

    // error message (only set when error = true)
    private String errorMessage;

    // success response
    public ChatResponse(String content) {
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isError = false;
    }

    // error response
    public ChatResponse(String content, boolean isError, String errorMessage) {
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isError = isError;
        this.errorMessage = errorMessage;
    }
}
