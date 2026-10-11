package com.bukimind.book.form;

import lombok.Data;

@Data
public class BookFilterForm {

    // Search by specific fields
    private String titleSearch;
    private String authorSearch;
    private String categorySearch;

    // ID range
    private Integer minId;
    private Integer maxId;

    // Global keyword search (title OR author OR category)
    private String search;
}
