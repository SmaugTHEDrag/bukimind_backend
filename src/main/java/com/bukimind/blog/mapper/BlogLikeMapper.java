package com.bukimind.blog.mapper;

import com.bukimind.blog.dto.BlogLikeDTO;
import com.bukimind.blog.entity.BlogLike;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BlogLikeMapper {
    BlogLikeDTO toDTO(BlogLike blogLike);
}
