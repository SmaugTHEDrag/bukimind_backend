package com.bukimind.blog.service;

import com.bukimind.blog.dto.BlogDTO;
import com.bukimind.blog.dto.BlogRequestDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IBlogService {

    List<BlogDTO> getAllBlogs();

    BlogDTO getBlogById(int id);

    BlogDTO createBlog(BlogRequestDTO requestDTO);

    // only blog owner can update (enforced with @PreAuthorize in the controller)
    BlogDTO updateBlog(int id, BlogRequestDTO requestDTO);

    // blog owner and admin can delete (enforced with @PreAuthorize in the controller)
    void deleteBlog(int id);

    // create blog and upload image if provided
    BlogDTO createBlogWithUpload(String title, String content, MultipartFile image, String imageURL);
}

