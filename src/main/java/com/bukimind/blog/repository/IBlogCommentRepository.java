package com.bukimind.blog.repository;

import com.bukimind.blog.entity.Blog;
import com.bukimind.blog.entity.BlogComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IBlogCommentRepository extends JpaRepository<BlogComment, Integer> {

    // top-level comments of a blog (no replies)
    List<BlogComment> findAllByBlogAndParentCommentIsNull(Blog blog);

    // all comments of a blog (have replies)
    List<BlogComment> findAllByBlog(Blog blog);
}
