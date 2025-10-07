package com.task7.model;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Display the create user form
     * CSRF token is automatically added by Spring Security
     */
    @GetMapping("/create")
    public String showCreateForm(Model model, HttpServletRequest request) {
        model.addAttribute("userDTO", new UserDTO());

        // Optional: You can manually add CSRF token to model if needed
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }

        return "create-user";
    }

    /**
     * Handle user creation POST request
     * Spring Security automatically validates CSRF token
     */
    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute("userDTO") UserDTO userDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            return "create-user";
        }

        try {
            // Create user
            User createdUser = userService.createUser(userDTO);

            // Add success message
            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + createdUser.getUsername() + "' created successfully!");

            return "redirect:/users/list";

        } catch (RuntimeException e) {
            // Handle errors (username/email already exists)
            model.addAttribute("errorMessage", e.getMessage());
            return "create-user";
        }
    }

    /**
     * List all users
     */
    @GetMapping("/list")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "user-list";
    }

    /**
     * Get user details by ID
     */
    @GetMapping("/{id}")
    public String getUserDetails(@PathVariable Long id, Model model) {
        try {
            User user = userService.getUserById(id);
            model.addAttribute("user", user);
            return "user-details";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/users/list";
        }
    }
}