package com.bukimind.blog.controller;

import com.bukimind.blog.dto.BlogCommentDTO;
import com.bukimind.blog.dto.BlogCommentRequestDTO;
import com.bukimind.blog.service.IBlogCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
@Tag(name = "Blog Comment API", description = "APIs for blog comments")
@RequiredArgsConstructor
public class BlogCommentController {

    private final IBlogCommentService commentService;

    @Operation(summary = "Get comments for a blog")
    @GetMapping("/{blogId}/comments")
    public ResponseEntity<List<BlogCommentDTO>> getComments(@PathVariable Integer blogId) {
        return ResponseEntity.ok(commentService.getCommentsByBlog(blogId));
    }

    @Operation(summary = "Add a comment")
    @PostMapping("/{blogId}/comments")
    public ResponseEntity<BlogCommentDTO> addComment(@RequestBody @Valid BlogCommentRequestDTO request) {
        return ResponseEntity.ok(commentService.addComment(request));
    }

    @Operation(summary = "Update comment", description = "Only Comment owner can update a comment")
    @PreAuthorize("@commentSecurity.canEdit(#commentId, authentication.name)")
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<BlogCommentDTO> updateComment(@PathVariable Integer commentId, @RequestBody @Valid BlogCommentRequestDTO request) {
        return ResponseEntity.ok(commentService.updateComment(commentId, request));
    }

    @Operation(summary = "Delete comment", description = "Only Blog owner, Admin and Comment owner can delete a comment")
    @PreAuthorize("hasRole('ADMIN') or @commentSecurity.canDelete(#commentId, authentication.name)")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Integer commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}

