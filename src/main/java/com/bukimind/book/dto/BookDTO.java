package com.bukimind.book.dto;

import com.bukimind.review.dto.ReviewDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {

    private Integer id;
    private String title;
    private String author;
    private String category;
    private String image;
    private String description;
    private String pdf;

    // average rating (1–5)
    private Double avgRating;

    // total number of reviews
    private Integer reviewCount;

    // list of reviews (only for detail view)
    private List<ReviewDTO> reviews;
}
