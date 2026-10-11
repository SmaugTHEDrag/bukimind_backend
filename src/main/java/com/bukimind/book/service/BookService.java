package com.bukimind.book.service;

import com.bukimind.book.dto.BookDTO;
import com.bukimind.book.dto.BookPageResponse;
import com.bukimind.book.dto.BookRequestDTO;
import com.bukimind.book.entity.Book;
import com.bukimind.review.entity.Review;
import com.bukimind.common.exception.ResourceNotFoundException;
import com.bukimind.book.form.BookFilterForm;
import com.bukimind.book.mapper.BookMapper;
import com.bukimind.review.mapper.ReviewMapper;
import com.bukimind.book.repository.IBookRepository;
import com.bukimind.review.repository.IReviewRepository;
import com.bukimind.ai.chatbox.service.FileUploadService;
import com.bukimind.book.specification.BookSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookService implements IBookService {

    private final IBookRepository bookRepository;

    private final BookMapper bookMapper;

    private final FileUploadService fileUploadService;

    private final IReviewRepository reviewRepository;

    private final ReviewMapper reviewMapper;

    // map Book entity to DTO with rating & reviews
    private BookDTO mapToBookDTOWithRating(Book book) {
        BookDTO dto = bookMapper.toDTO(book);

        // average rating
        Double avgRating = reviewRepository.findAvgRatingByBookId(book.getId());
        dto.setAvgRating(avgRating != null ? avgRating : 0.0);

        // total reviews
        Integer reviewCount = reviewRepository.getReviewCountByBookId(book.getId());
        dto.setReviewCount(reviewCount !=null ? reviewCount:0);

        // load reviews with user info
        List<Review> reviews = reviewRepository.findByBookIdWithUser(book.getId());
        dto.setReviews(reviewMapper.toListDTO(reviews));

        return dto;
    }

    @Override
    public BookPageResponse getAllBooks(BookFilterForm form, Pageable pageable) {
        Specification<Book> where = BookSpecification.buildWhere(form);

        Page<Book> books = bookRepository.findAll(where, pageable);

        Page<BookDTO> bookDTOs = books.map(this::mapToBookDTOWithRating);

        return new BookPageResponse(
            bookDTOs.getContent(),
            bookDTOs.getNumber(),
            bookDTOs.getTotalElements(),
            bookDTOs.getTotalPages(),
            bookDTOs.getSize(),
            bookDTOs.isLast(),
            bookDTOs.isFirst()
        );
    }

    @Override
    public BookDTO getBookById(int id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        return mapToBookDTOWithRating(book);
    }

    @Override
    public BookDTO createBook(BookRequestDTO bookRequestDTO) {
        Book book = bookMapper.toEntity(bookRequestDTO);
        Book savedBook = bookRepository.save(book);
        return bookMapper.toDTO(savedBook);
    }

    @Override
    public BookDTO updateBook(int id, BookRequestDTO bookRequestDTO) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        bookMapper.updateEntityFromDTO(bookRequestDTO, existingBook);

        Book updatedBook = bookRepository.save(existingBook);
        return bookMapper.toDTO(updatedBook);
    }

    @Override
    public void deleteBook(int id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found");
        }
        bookRepository.deleteById(id);
    }

    // create book with file upload (image + pdf)
    @Override
    public BookDTO createBookWithUpload(String title, String author, String category, String description, MultipartFile image, MultipartFile pdf) {
        try {
            String pdfUrl = fileUploadService.uploadFile(pdf, "books/pdfs");

            // Upload image if provided
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                imageUrl = fileUploadService.uploadFile(image, "books/images");
            }

            // Build request DTO
            BookRequestDTO dto = new BookRequestDTO(
                    title,
                    author,
                    category,
                    imageUrl,
                    description,
                    pdfUrl
            );
            return createBook(dto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
