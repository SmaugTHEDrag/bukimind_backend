package com.bukimind.ai.chatbox.service;

import com.bukimind.ai.chatbox.dto.ChatRequest;
import com.bukimind.ai.chatbox.dto.ChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class ChatService {
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SystemPromptService systemPromptService;

    public ChatService(RestTemplate restTemplate, ObjectMapper objectMapper, SystemPromptService systemPromptService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.systemPromptService = systemPromptService;
    }

    // Generates a response from the Gemini API based on the given user message and optional chat history.
    public ChatResponse generateResponse(ChatRequest chatRequest) {
        try {
            // Build request payload for Gemini API with system prompt
            Map<String, Object> requestPayload = buildGeminiRequest(chatRequest);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

            // Make API call
            String url = apiUrl + "?key=" + apiKey;
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Parse response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String generatedText = extractGeneratedText(responseJson);

            // Clean up response text (remove Markdown formatting)
            String cleanedText = cleanResponseText(generatedText);

            return new ChatResponse(cleanedText);

        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            return new ChatResponse(
                    "Sorry, I'm having trouble processing your request right now. However, I can still help you search for books by title, author, or genre!",
                    true,
                    e.getMessage()
            );
        }
    }

    // Builds the request payload for the Gemini API, including system prompt, chat history, and user message.
    private Map<String, Object> buildGeminiRequest(ChatRequest chatRequest) {
        Map<String, Object> request = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();

        // Check if this is the first message in conversation (no chat history)
        boolean isFirstMessage = chatRequest.getChatHistory() == null || chatRequest.getChatHistory().isEmpty();

        // Add chat history if present
        if (chatRequest.getChatHistory() != null && !chatRequest.getChatHistory().isEmpty()) {
            for (ChatRequest.ChatHistory history : chatRequest.getChatHistory()) {
                // Skip system messages from frontend - we'll handle system prompt differently
                if ("system".equals(history.getRole())) {
                    continue;
                }

                Map<String, Object> content = new HashMap<>();
                String geminiRole = mapToGeminiRole(history.getRole());
                content.put("role", geminiRole);

                List<Map<String, Object>> parts = new ArrayList<>();
                for (ChatRequest.ChatHistory.Part part : history.getParts()) {
                    // Add text part if present
                    if (part.getText() != null) {
                        Map<String, Object> textPart = new HashMap<>();
                        textPart.put("text", part.getText());
                        parts.add(textPart);
                    }
                    // Add inline_data part if present
                    if (part.getInline_data() != null) {
                        Map<String, Object> inlinePart = new HashMap<>();
                        Map<String, Object> inlineData = new HashMap<>();
                        inlineData.put("mime_type", part.getInline_data().getMime_type());
                        inlineData.put("data", part.getInline_data().getData());
                        inlinePart.put("inline_data", inlineData);
                        parts.add(inlinePart);
                    }
                }
                content.put("parts", parts);
                contents.add(content);
            }
        }

        // Prepare the user message with system context
        String enhancedMessage;
        if (isFirstMessage) {
            // For first message, include full system prompt
            enhancedMessage = systemPromptService.getContextualPrompt(chatRequest.getMessage());
        } else {
            // For subsequent messages, just add the user message with light context
            enhancedMessage = String.format(
                    "As the Book Assistant, please respond to: \"%s\"",
                    chatRequest.getMessage()
            );
        }

        // Add current user message
        Map<String, Object> userContent = new HashMap<>();
        userContent.put("role", "user");

        List<Map<String, Object>> userParts = new ArrayList<>();
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", enhancedMessage);
        userParts.add(textPart);

        // Add file data if present
        if (chatRequest.getFileData() != null) {
            Map<String, Object> filePart = new HashMap<>();
            Map<String, Object> inlineData = new HashMap<>();
            inlineData.put("mime_type", chatRequest.getFileData().getMime_type());
            inlineData.put("data", chatRequest.getFileData().getData());
            filePart.put("inline_data", inlineData);
            userParts.add(filePart);
        }

        userContent.put("parts", userParts);
        contents.add(userContent);

        request.put("contents", contents);

        // Add generation config for better responses
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 1024);
        generationConfig.put("topP", 0.8);
        generationConfig.put("topK", 40);
        request.put("generationConfig", generationConfig);

        return request;
    }

     // Maps various role names to Gemini-compatible roles
     // Gemini only accepts "user" and "model" as valid roles
    private String mapToGeminiRole(String role) {
        if (role == null) {
            return "user"; // Default fallback
        }

        String normalizedRole = role.toLowerCase().trim();

        switch (normalizedRole) {
            case "user":
            case "human":
                return "user";
            case "model":
            case "assistant":
            case "ai":
            case "bot":
            case "system":
                return "model";
            default:
                log.warn("Unknown role '{}', defaulting to 'user'", role);
                return "user";
        }
    }

    // Extracts the generated text from Gemini API response JSON.
    private String extractGeneratedText(JsonNode responseJson) {
        try {
            return responseJson
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            log.error("Error extracting text from Gemini response", e);
            throw new RuntimeException("Failed to parse Gemini response");
        }
    }

    // Cleans up the AI-generated text by removing Markdown formatting and trimming whitespace.
    private String cleanResponseText(String text) {
        if (text == null) return "";

        // Remove markdown bold formatting and clean up
        return text.replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                .replaceAll("\\*(.*?)\\*", "$1")
                .trim();
    }
}