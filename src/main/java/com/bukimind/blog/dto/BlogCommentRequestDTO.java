package com.bukimind.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogCommentRequestDTO {

    @NotNull(message = "Blog ID is required")
    private Integer blogId;

    // used when replying to another comment
    private Integer parentCommentId;

    @NotBlank(message = "Comment content is required")
    private String content;

    private String image;
}
