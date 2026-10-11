package com.bukimind.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Data Transfer Object (DTO) for creating or updating a blog
 * Receives input from client (request body) for blog management operations
 * Validation ensures input data is correct before service processing
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogRequestDTO {

    @NotBlank(message = "Title cannot be blank")
    private String title;
    @NotBlank(message = "Content cannot be blank")
    private String content;
    private String image;
}
