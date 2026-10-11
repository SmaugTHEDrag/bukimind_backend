package com.bukimind.ai.chatbox.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {

    // current user message
    private String message;

    // previous messages for conversation context
    private List<ChatHistory> chatHistory;

    // optional file attached with the message
    private FileData fileData;

    // represents a single message in the conversation history
    @Data
    public static class ChatHistory {

        // sender role: USER or MODEL
        private String role;

        // message content (can be text or inline data)
        private List<Part> parts;

        // a part of a message
        @Data
        public static class Part {

            // text content
            private String text;

            // inline binary data (image, pdf, ...)
            private InlineData inline_data;
        }

        // inline binary data inside a message
        @Data
        public static class InlineData {

            // mime type (image/png, application/pdf, ...)
            private String mime_type;

            // base64 encoded data
            private String data;
        }
    }

    // file attached to the current chat request
    @Data
    public static class FileData {

        // mime type of the file
        private String mime_type;

        // base64 encoded file data
        private String data;
    }
}
