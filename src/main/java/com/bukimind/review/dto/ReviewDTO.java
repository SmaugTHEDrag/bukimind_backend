package com.bukimind.review.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Integer id;
    private Integer bookId;
    private String title;

    private Integer rating;
    private String comment;

    private String username;

    private LocalDateTime createdAt;
}
