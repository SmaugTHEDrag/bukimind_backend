package com.bukimind.common.security;

import com.bukimind.blog.entity.Blog;
import com.bukimind.blog.repository.IBlogRepository;
import com.bukimind.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Ownership is decided on the stable identity (Keycloak subject), not on the username.
 */
@ExtendWith(MockitoExtension.class)
class BlogSecurityTest {

    @Mock
    private IBlogRepository blogRepository;

    @InjectMocks
    private BlogSecurity blogSecurity;

    private Blog blogOf(String keycloakUserId) {
        User owner = new User();
        owner.setKeycloakUserId(keycloakUserId);

        Blog blog = new Blog();
        blog.setId(1);
        blog.setUser(owner);
        return blog;
    }

    @Test
    void ownerMatchesByKeycloakUserId() {
        when(blogRepository.findById(1)).thenReturn(Optional.of(blogOf("kc-owner")));

        assertThat(blogSecurity.isOwner(1, "kc-owner")).isTrue();
    }

    @Test
    void anotherUserIsNotTheOwner() {
        when(blogRepository.findById(1)).thenReturn(Optional.of(blogOf("kc-owner")));

        assertThat(blogSecurity.isOwner(1, "kc-someone-else")).isFalse();
    }

    @Test
    void missingIdentityIsNotTheOwner() {
        assertThat(blogSecurity.isOwner(1, null)).isFalse();
    }
}
