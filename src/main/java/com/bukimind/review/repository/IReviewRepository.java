package com.bukimind.review.repository;

import com.bukimind.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IReviewRepository extends JpaRepository<Review, Integer> {

    // all reviews by a user
    List<Review> findByUserId(Integer userId);

    // all reviews for a book
    List<Review> findByBookId(Integer bookId);

    // review by user for a book (prevent duplicates)
    Optional<Review> findByUserIdAndBookId(Integer userId, Integer bookId);

    // check if user already reviewed a book
    boolean existsByUserIdAndBookId(Integer userId, Integer bookId);

    // average rating of a book
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double findAvgRatingByBookId(@Param("bookId") Integer bookId);

    // total number of reviews for a book
    @Query("SELECT COUNT(r.id) FROM Review r WHERE r.book.id = :bookId")
    Integer getReviewCountByBookId(@Param("bookId") Integer bookId);

    // all reviews with user info (avoid N+1)
    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.book.id = :bookId")
    List<Review> findByBookIdWithUser(@Param("bookId") Integer bookId);

}
