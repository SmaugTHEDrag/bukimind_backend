package com.bukimind.favorite.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteRequestDTO {
    @NotNull(message = "Book ID can not be null")
    private Integer bookId;
}
