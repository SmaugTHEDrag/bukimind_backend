package com.bukimind.ai.chatbox.controller;

import com.bukimind.ai.chatbox.dto.ChatRequest;
import com.bukimind.ai.chatbox.dto.ChatResponse;
import com.bukimind.ai.chatbox.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat API", description = "APIs for Book Assistant")
public class ChatController {

    private final ChatService geminiService;

    // simple health check
    @Operation(summary = "Health check")
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Chat service is running");
    }

    // generates chat response from the Book Assistant
    @Operation(summary = "Generate chat response")
    @PostMapping("/generate")
    public ResponseEntity<ChatResponse> generateChatResponse(@RequestBody ChatRequest chatRequest) {
        log.info("Received chat request: {}", chatRequest.getMessage());

        try {
            ChatResponse response = geminiService.generateResponse(chatRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating chat response", e);
            ChatResponse errorResponse = new ChatResponse(
                    "Sorry, I encountered an error while processing your request.",
                    true,
                    e.getMessage()
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

}
