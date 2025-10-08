package com.task9.controller;

import com.task9.model.User;
import com.task9.service.BookService;
import com.task9.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("totalBooks", bookService.getBooksByUser(user).size());
        model.addAttribute("availableBooks", bookService.getAvailableBooks().size());
        return "dashboard";
    }
}