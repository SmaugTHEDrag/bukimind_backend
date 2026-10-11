package com.bukimind.review.service;

import com.bukimind.review.dto.ReviewDTO;
import com.bukimind.review.dto.ReviewRequestDTO;
import com.bukimind.book.entity.Book;
import com.bukimind.review.entity.Review;
import com.bukimind.user.entity.User;
import com.bukimind.common.exception.ResourceNotFoundException;
import com.bukimind.common.security.CurrentUserService;
import com.bukimind.review.mapper.ReviewMapper;
import com.bukimind.book.repository.IBookRepository;
import com.bukimind.review.repository.IReviewRepository;
import com.bukimind.user.repository.IUserRepository;
import com.bukimind.ai.moderation.IAIModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService{

    private final IReviewRepository reviewRepository;

    private final IBookRepository bookRepository;

    private final IUserRepository userRepository;

    private final ReviewMapper reviewMapper;

    private final IAIModerationService moderationService;

    private final CurrentUserService currentUserService;

    @Override
    public List<ReviewDTO> getReviewsByBook(Integer bookId) {
        List<Review> reviews = reviewRepository.findByBookId(bookId);
        return reviewMapper.toListDTO(reviews);
    }

    // public profile lookup: username is the display key here, not the identity
    @Override
    public List<ReviewDTO> getReviewsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));
        List<Review> reviews = reviewRepository.findByUserId(user.getId());
        return reviewMapper.toListDTO(reviews);
    }

    @Override
    public ReviewDTO createReview(ReviewRequestDTO request) {
        User user = currentUserService.getCurrentUser();

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(()-> new ResourceNotFoundException("Book not found"));

        // one user can only review a book once
        if(reviewRepository.existsByUserIdAndBookId(user.getId(), book.getId())){
            throw new IllegalArgumentException("You already reviewed this book");
        }

        // check toxic content using external moderation
        moderationService.checkComment(request.getComment(), "Review contains inappropriate content");

        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toDTO(savedReview);
    }

    // ownership is enforced with @PreAuthorize in the controller
    @Override
    public ReviewDTO updateReview(Integer id, ReviewRequestDTO request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Review not found"));

        if(request.getRating() != null && (request.getRating()) != 0){
            review.setRating(request.getRating());
        }

        if(request.getComment() != null && !request.getComment().isBlank()){
            // check toxic content before update
            moderationService.checkComment(request.getComment(), "Review contains inappropriate content");
            review.setComment(request.getComment());
        }

        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.toDTO(updatedReview);
    }

    // ownership is enforced with @PreAuthorize in the controller
    @Override
    public void deleteReview(Integer id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Review not found"));

        reviewRepository.delete(review);
    }

}

