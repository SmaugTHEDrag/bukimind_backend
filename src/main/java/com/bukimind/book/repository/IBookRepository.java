package com.bukimind.book.repository;

import com.bukimind.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IBookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {
}
