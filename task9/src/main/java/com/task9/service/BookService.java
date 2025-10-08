package com.task9.service;

import com.task9.model.Book;
import com.task9.model.User;
import com.task9.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    // Get all books for specific user
    public List<Book> getBooksByUser(User user) {
        return bookRepository.findByUserId(user.getId());
    }

    // Get available books for specific user
    public List<Book> getAvailableBooksByUser(User user) {
        return bookRepository.findByUserIdAndAvailableTrue(user.getId());
    }

    // Search books by title for specific user
    public List<Book> searchBooksByUser(String title, User user) {
        return bookRepository.findByTitleContainingIgnoreCaseAndUserId(title, user.getId());
    }

    // Save book with user association
    public Book saveBook(Book book, User user) {
        book.setUser(user);
        return bookRepository.save(book);
    }

    // Check if book belongs to user
    public boolean isBookOwnedByUser(Long bookId, User user) {
        return bookRepository.existsByIdAndUserId(bookId, user.getId());
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    // Get book by ID only if it belongs to user
    public Book findByIdAndUser(Long id, User user) {
        return bookRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Book not found or access denied"));
    }
}