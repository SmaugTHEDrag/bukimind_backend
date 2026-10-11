package com.bukimind.security;

import com.bukimind.ai.chatbox.service.ChatService;
import com.bukimind.blog.controller.BlogController;
import com.bukimind.blog.controller.BlogLikeController;
import com.bukimind.blog.entity.Blog;
import com.bukimind.blog.repository.IBlogRepository;
import com.bukimind.blog.service.IBlogLikeService;
import com.bukimind.blog.service.IBlogService;
import com.bukimind.book.controller.BookController;
import com.bukimind.book.service.IBookService;
import com.bukimind.common.config.SecurityConfig;
import com.bukimind.common.exception.ResourceNotFoundException;
import com.bukimind.common.security.BlogSecurity;
import com.bukimind.user.controller.UserController;
import com.bukimind.user.dto.UserPageResponse;
import com.bukimind.user.entity.User;
import com.bukimind.user.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Authorization behaviour of the resource server:
 * 401 without a token, 403 for a role that is not allowed, public reads stay open
 * and ownership is enforced on the protected resource.
 *
 * The realm-role -> ROLE_* mapping itself is covered by
 * KeycloakJwtAuthenticationConverterTest; here the authorities are supplied directly.
 */
@WebMvcTest(
        controllers = {UserController.class, BlogController.class, BlogLikeController.class, BookController.class},
        properties = "app.cors.allowed-origins=http://localhost:3000"
)
@Import({SecurityConfig.class, BlogSecurity.class})
class SecurityAuthorizationTest {

    @TestConfiguration
    static class StubJwtDecoderConfiguration {
        // Keycloak is not running during tests; Spring Security only needs a decoder bean.
        @Bean
        JwtDecoder jwtDecoder() {
            return token -> {
                throw new UnsupportedOperationException("JWT decoding is not exercised in this test");
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserService userService;

    @MockBean
    private IBlogService blogService;

    @MockBean
    private IBlogLikeService blogLikeService;

    @MockBean
    private IBookService bookService;

    @MockBean
    private ChatService chatService;

    @MockBean
    private IBlogRepository blogRepository;

    // ------------------------------------------------------------------ 401

    @Test
    void adminEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void aiChatEndpointIsNoLongerPublic() throws Exception {
        mockMvc.perform(post("/api/chat/generate").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void currentUserLikeEndpointRequiresAToken() throws Exception {
        mockMvc.perform(get("/api/blogs/1/likes/has"))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------ 403

    @Test
    void customerCannotCallAnAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/users").with(customer()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void nonOwnerCannotUpdateABlog() throws Exception {
        User owner = new User();
        owner.setKeycloakUserId("kc-owner");
        Blog blog = new Blog();
        blog.setId(1);
        blog.setUser(owner);
        when(blogRepository.findById(1)).thenReturn(Optional.of(blog));

        mockMvc.perform(put("/api/blogs/1")
                        .with(customer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t\",\"content\":\"c\"}"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ 200

    @Test
    void adminCanCallAnAdminEndpoint() throws Exception {
        when(userService.getAllUsers(any(), any())).thenReturn(new UserPageResponse());

        mockMvc.perform(get("/api/users").with(admin()))
                .andExpect(status().isOk());
    }

    @Test
    void publicReadEndpointStaysOpen() throws Exception {
        when(blogService.getAllBlogs()).thenReturn(List.of());

        mockMvc.perform(get("/api/blogs"))
                .andExpect(status().isOk());
    }

    // ------------------------------------------- admin endpoints (ROLE_ check)

    @Test
    void customerCannotDeleteABook() throws Exception {
        mockMvc.perform(delete("/api/books/1").with(customer()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanDeleteABook() throws Exception {
        mockMvc.perform(delete("/api/books/1").with(admin()))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------- consistent error body

    @Test
    void missingResourceReturnsConsistentErrorBody() throws Exception {
        when(blogService.getBlogById(999)).thenThrow(new ResourceNotFoundException("Blog not found"));

        mockMvc.perform(get("/api/blogs/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Blog not found"));
    }

    private static RequestPostProcessor customer() {
        return jwt()
                .jwt(builder -> builder.subject("kc-customer"))
                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    private static RequestPostProcessor admin() {
        return jwt()
                .jwt(builder -> builder.subject("kc-admin"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
