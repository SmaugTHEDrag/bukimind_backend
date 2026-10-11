package com.bukimind.blog.controller;

import com.bukimind.blog.dto.BlogDTO;
import com.bukimind.blog.dto.BlogRequestDTO;
import com.bukimind.blog.service.IBlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
@Tag(name = "Blog API", description = "APIs for blogs")
@RequiredArgsConstructor
public class BlogController {

    private final IBlogService blogService;

    @Operation(summary = "Get all blogs")
    @GetMapping
    public ResponseEntity<List<BlogDTO>> getAllBlogs() {
        return ResponseEntity.ok(blogService.getAllBlogs());
    }

    @Operation(summary = "Get blog by ID")
    @GetMapping("/{id}")
    public ResponseEntity<BlogDTO> getBlogById(@PathVariable int id) {
        return ResponseEntity.ok(blogService.getBlogById(id));
    }

    @Operation(summary = "Create a blog")
    @PostMapping
    public ResponseEntity<BlogDTO> createBlog(@Valid @RequestBody BlogRequestDTO blogRequestDTO) {
        BlogDTO blogDTO = blogService.createBlog(blogRequestDTO);
        return ResponseEntity.ok(blogDTO);
    }

    @Operation(summary = "Create blog with image")
    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<BlogDTO> createBlogWithUpload(
            @RequestPart("title") String title,
            @RequestPart("content") String content,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "imageURL", required = false) String imageURL
    ) {
        BlogDTO created = blogService.createBlogWithUpload(title, content, image, imageURL);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update a blog", description = "Only Blog owner can update a blog")
    @PreAuthorize("@blogSecurity.isOwner(#id, authentication.name)")
    @PutMapping("/{id}")
    public ResponseEntity<BlogDTO> updateBlog(@PathVariable int id, @RequestBody @Valid BlogRequestDTO blogRequestDTO) {
        BlogDTO blogDTO = blogService.updateBlog(id, blogRequestDTO);
        return ResponseEntity.ok(blogDTO);
    }

    @Operation(summary = "Delete a blog", description = "Only Admin and Blog owner can delete a blog")
    @PreAuthorize("hasRole('ADMIN') or @blogSecurity.isOwner(#id, authentication.name)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable int id) {
        blogService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }
}
