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
            return "Authentication/register";
        }

        try {
            userService.registerUser(user);

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            // ✅ Chuyển hướng sau khi đăng nhập thành công
            return "redirect:/"; // hoặc "redirect:/dashboard" tùy app của bạn
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "Authentication/register";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", "An error occurred: " + e.getMessage());
            return "Authentication/register";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred.");
            return "Authentication/register";
        }
    }

}
