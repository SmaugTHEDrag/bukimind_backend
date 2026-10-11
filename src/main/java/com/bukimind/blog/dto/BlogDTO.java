package com.bukimind.blog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/*
 * Data Transfer Object (DTO) for Blog entity
 * Used to transfer blog information from backend to client
 */
@Data
public class BlogDTO {

    private Integer id;
    private String title;
    private String content;
    private String image;
    private String username;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // total number of likes
    private Long likeCount;

    // list of comment
    private List<BlogCommentDTO> comments;
}
