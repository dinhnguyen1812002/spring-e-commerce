package com.app.e_commerce.controller;

import com.app.e_commerce.entity.User;
import com.app.e_commerce.services.UserService;
import com.app.e_commerce.services.UserServiceOptimize;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    UserServiceOptimize userService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "Authentication/Login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "Authentication/register"; // This will render the register.html Thymeleaf template
    }

    // Handle the registration form submission
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            // If there are validation errors, re-display the form with error messages
            return "Authentication/register";
        }

        try {
            // Attempt to register the user
            userService.registerUser(user);

            // Automatically log in the user
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            // Use the constructor that doesn't require credentials since we're auto-authenticating after registration
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null, // Don't provide credentials here as they're already verified
                userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Redirect to the home page after successful registration and login
            return "redirect:/login?success=true";
        } catch (IllegalArgumentException e) {
            // Handle case where username or email is already taken
            model.addAttribute("errorMessage", e.getMessage());
            return "Authentication/register";
        } catch (IllegalStateException e) {
            // Handle case where the USER role is not found in the database
            model.addAttribute("errorMessage", "An error occurred: " + e.getMessage());
            return "Authentication/register";
        } catch (Exception e) {
            // Generic error handling for unexpected issues
            model.addAttribute("errorMessage", "An unexpected error occurred.");
            return "Authentication/register";
        }
    }

}
