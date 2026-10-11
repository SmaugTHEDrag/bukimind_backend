package com.bukimind.review.controller;

import com.bukimind.review.dto.ReviewDTO;
import com.bukimind.review.dto.ReviewRequestDTO;
import com.bukimind.review.service.IReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Review API", description = "APIs for reviews of a book")
@RequestMapping("api/reviews")
public class ReviewController {
    private final IReviewService reviewService;

    @Operation(summary = "Get reviews by book")
    @GetMapping("book/{bookId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByBook(@PathVariable Integer bookId){
        return ResponseEntity.ok(reviewService.getReviewsByBook(bookId));
    }

    @Operation(summary = "Get reviews by user")
    @GetMapping("/{username}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByUser(@PathVariable String username){
        return ResponseEntity.ok(reviewService.getReviewsByUser(username));
    }

    @Operation(summary = "Create a review")
    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(@RequestBody @Valid ReviewRequestDTO requestDTO){
        return ResponseEntity.ok(reviewService.createReview(requestDTO));
    }

    @Operation(summary = "Update review", description = "Only review owner can update review")
    @PreAuthorize("@reviewSecurity.isOwner(#id, authentication.name)")
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> updateReview(@PathVariable Integer id, @RequestBody @Valid ReviewRequestDTO reviewRequestDTO){
        return ResponseEntity.ok(reviewService.updateReview(id, reviewRequestDTO));
    }

    @Operation(summary = "Delete review", description = "Only review owner can delete review")
    @PreAuthorize("@reviewSecurity.isOwner(#id, authentication.name)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ReviewDTO> deleteReview(@PathVariable Integer id){
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

}

