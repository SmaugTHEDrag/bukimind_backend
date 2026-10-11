package com.bukimind.blog.service;

import com.bukimind.blog.dto.BlogLikeDTO;
import com.bukimind.blog.entity.Blog;
import com.bukimind.blog.entity.BlogLike;
import com.bukimind.user.entity.User;
import com.bukimind.common.exception.ResourceNotFoundException;
import com.bukimind.common.security.CurrentUserService;
import com.bukimind.blog.mapper.BlogLikeMapper;
import com.bukimind.blog.repository.IBlogLikeRepository;
import com.bukimind.blog.repository.IBlogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BlogLikeService implements IBlogLikeService{

    private final IBlogLikeRepository likeRepository;

    private final IBlogRepository blogRepository;

    private final CurrentUserService currentUserService;

    private final BlogLikeMapper blogLikeMapper;

    @Override
    public BlogLikeDTO likeBlog(Integer blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

        User user = currentUserService.getCurrentUser();

        Optional<BlogLike> existing = likeRepository.findByBlogAndUser(blog, user);
        if (existing.isPresent()) {
            return blogLikeMapper.toDTO(existing.get());
        }

        BlogLike like = new BlogLike();
        like.setBlog(blog);
        like.setUser(user);

        BlogLike saved = likeRepository.save(like);
        return blogLikeMapper.toDTO(saved);
    }


    @Override
    public void unlikeBlog(Integer blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

        User user = currentUserService.getCurrentUser();

        BlogLike like = likeRepository.findByBlogAndUser(blog, user)
                .orElseThrow(() -> new ResourceNotFoundException("Like not found"));

        likeRepository.delete(like);
    }

    @Override
    public long getLikeCount(Integer blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));
        return likeRepository.countByBlog(blog);
    }

    @Override
    public boolean hasUserLiked(Integer blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));
        return likeRepository.existsByBlogAndUser(blog, currentUserService.getCurrentUser());
    }

}
