package com.bukimind.blog.mapper;

import com.bukimind.blog.dto.BlogCommentDTO;
import com.bukimind.blog.entity.BlogComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BlogCommentMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "blogId", source = "blog.id")
    BlogCommentDTO toDTO(BlogComment blogComment);
}
