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

import java.time.LocalDate;

@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String books(Model model, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        model.addAttribute("books", bookService.getBooksByUser(user));
        model.addAttribute("book", new Book());
        return "books";
    }

    @PostMapping("/add")
    public String addBook(@ModelAttribute Book book, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        book.setUser(user);
        book.setAddedDate(LocalDate.now());
        bookService.saveBook(book);
        return "redirect:/books";
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return "redirect:/books";
    }

    @GetMapping("/search")
    public String searchBooks(@RequestParam String title, Model model, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        model.addAttribute("books", bookService.searchBooks(title));
        model.addAttribute("book", new Book());
        model.addAttribute("searchTerm", title);
        return "books";
    }
}