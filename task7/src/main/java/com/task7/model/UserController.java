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
     * Display registration form
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model, HttpServletRequest request) {
        if (!model.containsAttribute("userDTO")) {
            model.addAttribute("userDTO", new UserDTO());
        }

        // Add CSRF token information
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }

        return "register";
    }

    /**
     * Handle user registration
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userDTO") UserDTO userDTO,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model,
                               HttpServletRequest request) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("userDTO", userDTO);
            return "register";
        }

        try {
            // Create user
            User createdUser = userService.createUser(userDTO);

            // Add success message
            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + createdUser.getUsername() + "' registered successfully! Please login.");

            return "redirect:/login";

        } catch (IllegalArgumentException | UserAlreadyExistsException e) {
            // Handle validation and uniqueness errors
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("userDTO", userDTO);
            return "register";
        }
    }

    /**
     * Display the create user form (alternative registration)
     */
    @GetMapping("/create")
    public String showCreateForm(Model model, HttpServletRequest request) {
        if (!model.containsAttribute("userDTO")) {
            model.addAttribute("userDTO", new UserDTO());
        }

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }

        return "create-user";
    }

    /**
     * Handle user creation POST request
     */
    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute("userDTO") UserDTO userDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        if (bindingResult.hasErrors()) {
            return "create-user";
        }

        try {
            User createdUser = userService.createUser(userDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + createdUser.getUsername() + "' created successfully! Please login.");
            return "redirect:/login";

        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "create-user";
        }
    }

    /**
     * List all users (requires authentication)
     */
    @GetMapping("/list")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "user-list";
    }

    /**
     * User profile page (requires authentication)
     */
    @GetMapping("/profile")
    public String userProfile(Model model) {
        // In a real application, you'd get the current user from security context
        model.addAttribute("message", "User Profile Page - This would show current user's information");
        return "user-profile";
    }
}