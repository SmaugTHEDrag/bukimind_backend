package com.bukimind.blog.mapper;

import com.bukimind.blog.dto.BlogCommentDTO;
import com.bukimind.blog.dto.BlogDTO;
import com.bukimind.blog.dto.BlogRequestDTO;
import com.bukimind.blog.entity.Blog;
import com.bukimind.blog.entity.BlogComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = BlogCommentMapper.class)
public interface BlogMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "blogId", source = "blog.id")
    BlogCommentDTO toCommentDto(BlogComment comment);

    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "username", source = "user.username")
    BlogDTO toDTO(Blog blog);

    Blog toEntity(BlogRequestDTO blogRequestDTO);
}
