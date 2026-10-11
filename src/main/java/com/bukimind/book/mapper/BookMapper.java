package com.bukimind.book.mapper;

import com.bukimind.book.dto.BookDTO;
import com.bukimind.book.dto.BookRequestDTO;
import com.bukimind.book.entity.Book;
import com.bukimind.review.mapper.ReviewMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = ReviewMapper.class)
public interface BookMapper {

    @Mapping(source = "reviews", target = "reviews")
    BookDTO toDTO(Book book);

    Book toEntity(BookRequestDTO bookRequestDTO);

    void updateEntityFromDTO(BookRequestDTO bookRequestDTO,@MappingTarget Book existingBook);
}
