package com.bukimind.blog.repository;

import com.bukimind.blog.entity.Blog;
import com.bukimind.blog.entity.BlogLike;
import com.bukimind.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IBlogLikeRepository extends JpaRepository<BlogLike, Integer> {

    // find like by blog and user
    Optional<BlogLike> findByBlogAndUser(Blog blog, User user);

    // check if user already liked the blog
    boolean existsByBlogAndUser(Blog blog, User user);

    // count total likes for a blog
    long countByBlog(Blog blog);

    // all likes of a blog
    List<BlogLike> findAllByBlog(Blog blog);
}
