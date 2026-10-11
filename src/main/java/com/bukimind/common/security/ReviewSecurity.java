package com.bukimind.common.security;

import com.bukimind.review.repository.IReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("reviewSecurity")
@RequiredArgsConstructor
public class ReviewSecurity {

    private final IReviewRepository reviewRepository;

    // only the review owner may change it
    public boolean isOwner(Integer reviewId, String keycloakUserId) {
        if (reviewId == null || keycloakUserId == null) return false;

        return reviewRepository.findById(reviewId)
                .map(review -> keycloakUserId.equals(review.getUser().getKeycloakUserId()))
                .orElse(false);
    }
}
