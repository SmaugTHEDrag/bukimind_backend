package com.bukimind.common.security;

import com.bukimind.blog.repository.IBlogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("blogSecurity")
@RequiredArgsConstructor
public class BlogSecurity {

    private final IBlogRepository blogRepository;

    // check if the given blog belongs to the current Keycloak user
    public boolean isOwner(int blogId, String keycloakUserId) {
        if (keycloakUserId == null) return false;

        return blogRepository.findById(blogId)
                .map(blog -> keycloakUserId.equals(blog.getUser().getKeycloakUserId()))
                .orElse(false);
    }
}

