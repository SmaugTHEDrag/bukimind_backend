package com.bukimind.common.security;

import com.bukimind.blog.repository.IBlogCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("commentSecurity")
@RequiredArgsConstructor
public class CommentSecurity {

    private final IBlogCommentRepository commentRepository;

    // comment owner or blog owner can delete the comment
    public boolean canDelete(Integer commentId, String keycloakUserId) {
        if (commentId == null || keycloakUserId == null) return false;

        return commentRepository.findById(commentId)
                .map(comment ->
                        keycloakUserId.equals(comment.getUser().getKeycloakUserId()) || // Comment owner
                                keycloakUserId.equals(comment.getBlog().getUser().getKeycloakUserId()) // Blog owner
                )
                .orElse(false);
    }

    // only comment owner can edit
    public boolean canEdit(Integer commentId, String keycloakUserId) {
        if (commentId == null || keycloakUserId == null) return false;

        return commentRepository.findById(commentId)
                .map(comment -> keycloakUserId.equals(comment.getUser().getKeycloakUserId())) // Only comment owner
                .orElse(false);
    }
}


