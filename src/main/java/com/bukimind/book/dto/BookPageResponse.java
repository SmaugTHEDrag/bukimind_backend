package com.bukimind.book.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookPageResponse {

    // books in the current page
    private List<BookDTO> content;

    // current page index (0-based)
    private int currentPage;

    // total number of books
    private long totalItems;

    // total number of pages
    private int totalPages;

    // page size
    private int pageSize;

    // first / last page flags
    private boolean isLast;
    private boolean isFirst;
}
