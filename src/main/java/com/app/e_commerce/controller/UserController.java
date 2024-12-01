package com.app.e_commerce.controller;

import com.app.e_commerce.entity.User;

import com.app.e_commerce.repository.UserRepo;
import com.app.e_commerce.services.UserService;
import com.app.e_commerce.services.UserServiceOptimize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

//    @Autowired
//    private UserService userService;
    @Autowired
    private UserServiceOptimize userService;

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/edit/{userId}")
    public String showEditUserForm(@PathVariable UUID userId, Model model, Principal pro) {
        if(pro == null){
            return "redirect:/auth/login";
        }
        User user = userService.getUserById(userId);
        model.addAttribute("user", user);
        return "users/user-profile";  // Points to user-edit.html Thymeleaf template
    }

    @PostMapping("/edit/{userId}")
    public String updateUser(@PathVariable UUID userId,
                             @ModelAttribute User user,
                             @RequestParam("avatarFile") MultipartFile avatarFile,
                             RedirectAttributes redirectAttributes,
                             Principal pri
    ) {

        if(pri ==null) return "redirect:/auth/login";
        // Update the user's information and avatar
        userService.updateUser(userId, user, avatarFile);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");

        // Reload the same profile page with updated information
        return "redirect:/users/edit/";
    }

}
