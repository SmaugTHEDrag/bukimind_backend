package com.bukimind.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookRequestDTO {

    @NotBlank(message = "Title must not be blank")
    @Size(min = 1, max = 255, message = "Title length must be between 2 and 255 characters")
    private String title;

    @NotBlank(message = "Author must not be blank")
    private String author;

    @NotBlank(message = "Category must not be blank")
    private String category;

    @Pattern(regexp = "^(https?|ftp)://.*$", message = "Image must be a valid URL")
    private String image;

    @Size(max = 5000, message = "Description must be less than 2000 characters")
    private String description;

    @Pattern(regexp = "^(https?|ftp)://.*\\.pdf$", message = "PDF must be a valid PDF URL")
    private String pdf;
}
