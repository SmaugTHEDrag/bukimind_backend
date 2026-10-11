package com.bukimind.book.service;

import com.bukimind.book.dto.BookDTO;
import com.bukimind.book.dto.BookPageResponse;
import com.bukimind.book.dto.BookRequestDTO;
import com.bukimind.book.form.BookFilterForm;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IBookService {

    BookPageResponse getAllBooks(BookFilterForm form, Pageable pageable);

    BookDTO getBookById(int id);

    BookDTO createBook(BookRequestDTO bookRequestDTO);

    BookDTO updateBook(int id, BookRequestDTO bookRequestDTO);

    void deleteBook(int id);

    // create book with image + pdf upload (cloud storage)
    BookDTO createBookWithUpload(String title, String author, String category, String description,
                                MultipartFile image, MultipartFile pdf);
}
