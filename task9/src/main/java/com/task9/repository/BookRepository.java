package com.task9.repository;

import com.task9.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    // Find all books by user ID
    List<Book> findByUserId(Long userId);

    // Find available books by user ID
    List<Book> findByUserIdAndAvailableTrue(Long userId);

    // Search books by title for specific user
    List<Book> findByTitleContainingIgnoreCaseAndUserId(String title, Long userId);

    // Check if book exists and belongs to user
    boolean existsByIdAndUserId(Long id, Long userId);

    // Find book by ID and user ID
    Optional<Book> findByIdAndUserId(Long id, Long userId);

    // Count books by user
    long countByUserId(Long userId);

    // Count available books by user
    long countByUserIdAndAvailableTrue(Long userId);
}