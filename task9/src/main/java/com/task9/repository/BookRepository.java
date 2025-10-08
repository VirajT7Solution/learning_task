package com.task9.repository;


import com.task9.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByUserId(Long userId);

    List<Book> findByAvailableTrue();

    List<Book> findByTitleContainingIgnoreCase(String title);
}