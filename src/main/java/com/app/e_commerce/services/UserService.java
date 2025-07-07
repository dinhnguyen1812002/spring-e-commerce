package com.app.e_commerce.services;

import com.app.e_commerce.entity.Role;
import com.app.e_commerce.entity.User;
import com.app.e_commerce.repository.RoleRepo;
import com.app.e_commerce.repository.UserRepo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class UserService {

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepo roleRepository;

    public UserService(UserRepo userRepository, PasswordEncoder passwordEncoder, RoleRepo roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }
    public List<User> getAllUser(){
        return userRepository.findAll();
    }
    /**
     * Registers a new user with a default avatar and a 'USER' role.
     */
    public User registerUser(User user) {
        // Check if the username or email is already taken
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already taken");
        }

        // Set the default role to 'USER'
        Optional<Role> userRole = roleRepository.findByName("USER");
        if (userRole.isPresent()) {
            user.setRoles(Collections.singleton(userRole.get()));
        } else {
            // If the USER role does not exist, throw an exception or create it
            throw new IllegalStateException("USER role not found in the database");
        }

        // Encode the user's password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set a default avatar as a URL or a string
        // Convert default avatar image to Base64 string
        try {
            ClassPathResource imgFile = new ClassPathResource("static/images/avatar.png"); // Ensure this path is correct
            byte[] imageBytes = Files.readAllBytes(imgFile.getFile().toPath());
            String base64Avatar = Base64.getEncoder().encodeToString(imageBytes);

            // Set the avatar to the Base64 string
            user.setAvatar(base64Avatar);
        } catch (IOException e) {
            throw new RuntimeException("Failed to set default avatar", e);
        }


        // Save the user to the database
        return userRepository.save(user);
    }

    /**
     * Updates an existing user. Only fields that are explicitly provided are updated.
     * Optionally, an avatar file can be uploaded.
     */
    public void updateUser(UUID userId, User updatedUser, MultipartFile avatarFile) throws IOException {
        // Fetch the existing user by ID
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update fields that are provided (username, email)
        if (updatedUser.getUsername() != null && !updatedUser.getUsername().isEmpty()) {
            existingUser.setUsername(updatedUser.getUsername());
        }

        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
            existingUser.setEmail(updatedUser.getEmail());
        }

        // Handle password update, but only if provided (password can be optional)
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword())); // Encode password
        }

        // Handle avatar file upload (if provided)
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Convert the file to Base64 string or store it as a URL if needed
            String avatarBase64 = java.util.Base64.getEncoder().encodeToString(avatarFile.getBytes());
            existingUser.setAvatar(avatarBase64);
        }

        // Save and return the updated user
        userRepository.save(existingUser);
    }

    /**
     * Fetches a user by their ID.
     */
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    public long countUsers() {
        return userRepository.count();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

}
