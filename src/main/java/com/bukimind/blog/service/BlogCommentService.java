package com.bukimind.blog.service;

import com.bukimind.blog.dto.BlogCommentDTO;
import com.bukimind.blog.dto.BlogCommentRequestDTO;
import com.bukimind.blog.entity.Blog;
import com.bukimind.blog.entity.BlogComment;
import com.bukimind.user.entity.User;
import com.bukimind.common.exception.ResourceNotFoundException;
import com.bukimind.common.security.CurrentUserService;
import com.bukimind.blog.mapper.BlogCommentMapper;
import com.bukimind.blog.repository.IBlogCommentRepository;
import com.bukimind.blog.repository.IBlogRepository;
import com.bukimind.ai.moderation.IAIModerationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BlogCommentService implements IBlogCommentService {

    private final IBlogCommentRepository commentRepository;

    private final IBlogRepository blogRepository;

    private final CurrentUserService currentUserService;

    private final BlogCommentMapper blogCommentMapper;

    private final IAIModerationService moderationService;

    // recursive mapping for nested replies
    private BlogCommentDTO mapToCommentDTO(BlogComment blogComment) {
        BlogCommentDTO dto = blogCommentMapper.toDTO(blogComment);

        if (blogComment.getUser() != null) {
            dto.setUsername(blogComment.getUser().getUsername());
        }

        dto.setBlogId(blogComment.getBlog() != null ? blogComment.getBlog().getId() : null);

        if (blogComment.getChildComment() != null) {
            dto.setReplies(blogComment.getChildComment().stream()
                    .map(this::mapToCommentDTO)
                    .toList());
        } else {
            dto.setReplies(Collections.emptyList());
        }

        return dto;
    }

    // add new comment or reply
    @Override
    public BlogCommentDTO addComment(BlogCommentRequestDTO request) {
        Blog blog = blogRepository.findById(request.getBlogId())
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

        User user = currentUserService.getCurrentUser();

        BlogComment comment = new BlogComment();
        comment.setBlog(blog);
        comment.setUser(user);

        if (request.getContent() != null && !request.getContent().isBlank()) {
            moderationService.checkComment(request.getContent(), "Comment contains inappropriate content");
            comment.setContent(request.getContent());
        }

        comment.setImage(request.getImage());

        if (request.getParentCommentId() != null) {
            BlogComment parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            comment.setParentComment(parent);
        }

        BlogComment saved = commentRepository.save(comment);
        return mapToCommentDTO(saved);
    }

    // update content or image only
    @Override
    public BlogCommentDTO updateComment(Integer commentId, BlogCommentRequestDTO request) {
        BlogComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (request.getContent() != null && !request.getContent().isBlank()) {
            moderationService.checkComment(request.getContent(), "Comment contains inappropriate content");
            comment.setContent(request.getContent());
        }

        if (request.getImage() != null) {
            comment.setImage(request.getImage());
        }

        BlogComment updated = commentRepository.save(comment);
        return mapToCommentDTO(updated);
    }


    // privileges are checked with @PreAuthorize before this is called
    @Override
    public void deleteComment(Integer commentId) {
        BlogComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        commentRepository.delete(comment);
    }


    // load top-level comments with replies
    @Override
    public List<BlogCommentDTO> getCommentsByBlog(Integer blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

        return commentRepository.findAllByBlogAndParentCommentIsNull(blog)
                .stream()
                .map(this::mapToCommentDTO)
                .toList();
    }
}

