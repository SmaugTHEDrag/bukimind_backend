package com.bukimind.blog.service;

import com.bukimind.blog.dto.BlogCommentDTO;
import com.bukimind.blog.dto.BlogCommentRequestDTO;

import java.util.List;

public interface IBlogCommentService {

    List<BlogCommentDTO> getCommentsByBlog(Integer blogId);

    // add new comment or reply
    BlogCommentDTO addComment(BlogCommentRequestDTO request);

    // only comment owner can update (enforced with @PreAuthorize in the controller)
    BlogCommentDTO updateComment(Integer commentId, BlogCommentRequestDTO request);

    // comment owner, blog owner or admin can delete (enforced with @PreAuthorize in the controller)
    void deleteComment(Integer commentId);

}

