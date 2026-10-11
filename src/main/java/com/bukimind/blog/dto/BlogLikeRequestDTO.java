package com.bukimind.blog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/*
 * Data Transfer Object (DTO) for creating a like on a blog
 * Receives input from client (request body) when a user likes a blog
 * Validation ensures input data is correct before service processing
 */
@Data
public class BlogLikeRequestDTO {
    @NotNull(message = "Blog ID is required")
    private Integer blogId;
}
