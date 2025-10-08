package com.task9.controller;

import com.task9.model.Book;
import com.task9.model.User;
import com.task9.service.BookService;
import com.task9.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String showUserBooks(Model model, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        model.addAttribute("books", bookService.getBooksByUser(user));
        model.addAttribute("book", new Book());
        return "books";
    }

    @PostMapping("/add")
    public String addBook(@ModelAttribute Book book,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        try {
            User user = getUserFromAuthentication(authentication);
            book.setAddedDate(LocalDate.now());
            book.setAvailable(true);
            bookService.saveBook(book, user);
            redirectAttributes.addFlashAttribute("success", "Book added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add book: " + e.getMessage());
        }
        return "redirect:/books";
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = getUserFromAuthentication(authentication);

            // Check if the book belongs to the logged-in user
            if (bookService.isBookOwnedByUser(id, user)) {
                bookService.deleteBook(id);
                redirectAttributes.addFlashAttribute("success", "Book deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this book!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete book: " + e.getMessage());
        }
        return "redirect:/books";
    }

    @GetMapping("/search")
    public String searchBooks(@RequestParam String title,
                              Model model,
                              Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        model.addAttribute("books", bookService.searchBooksByUser(title, user));
        model.addAttribute("book", new Book());
        model.addAttribute("searchTerm", title);
        return "books";
    }

    // Helper method to get user from authentication
    private User getUserFromAuthentication(Authentication authentication) {
        return userService.findByEmail(authentication.getName());
    }
}