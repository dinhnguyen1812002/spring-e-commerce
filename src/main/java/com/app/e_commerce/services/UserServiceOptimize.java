package com.app.e_commerce.services;

import com.app.e_commerce.entity.Role;
import com.app.e_commerce.entity.User;
import com.app.e_commerce.exception.ResourceNotFoundException;
import com.app.e_commerce.exception.UserAlreadyExistsException;
import com.app.e_commerce.repository.RoleRepo;
import com.app.e_commerce.repository.UserRepo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@Transactional
public class UserServiceOptimize {

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepo roleRepository;
    private final ImageService imageService;

    @Value("${app.default.avatar.path:static/images/avatar.png}")
    private String defaultAvatarPath;

    private static final String DEFAULT_ROLE = "USER";
    private static final int MAX_AVATAR_SIZE = 1024 * 1024; // 1MB

    public UserServiceOptimize  (UserRepo userRepository,
                       PasswordEncoder passwordEncoder,
                       RoleRepo roleRepository,
                       ImageService imageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.imageService = imageService;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void registerUser(@Valid @NotNull User user) {
        validateNewUser(user);
        setUserRole(user);
        encodePassword(user);
        setDefaultAvatar(user);

        try {
            userRepository.save(user);
        } catch (Exception e) {
            log.error("Error while registering user: {}", user.getUsername(), e);
            throw new RuntimeException("Failed to register user", e);
        }
    }

    @Transactional
    public User updateUser(UUID userId, User updatedUser, MultipartFile avatarFile) {
        User existingUser = getUserById(userId);

        updateUserFields(existingUser, updatedUser);

        if (avatarFile != null && !avatarFile.isEmpty()) {
            updateAvatar(existingUser, avatarFile);
        }

        try {
            return userRepository.save(existingUser);
        } catch (Exception e) {
            log.error("Error while updating user: {}", userId, e);
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    // Private helper methods
    private void validateNewUser(User user) {
        userRepository.findByUsername(user.getUsername()).ifPresent(u -> {
            throw new UserAlreadyExistsException("Username already taken: " + user.getUsername());
        });

        userRepository.findByEmail(user.getEmail()).ifPresent(u -> {
            throw new UserAlreadyExistsException("Email already taken: " + user.getEmail());
        });
    }

    private void setUserRole(User user) {
        Role userRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException(DEFAULT_ROLE + " role not found"));
        user.setRoles(Collections.singleton(userRole));
    }

    private void encodePassword(User user) {
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
    }

    private void setDefaultAvatar(User user) {
        try {
            String base64Avatar = imageService.getDefaultAvatarBase64();
            user.setAvatar(base64Avatar);
        } catch (IOException e) {
            log.error("Failed to set default avatar", e);
            throw new RuntimeException("Failed to set default avatar", e);
        }
    }

    private void updateUserFields(User existingUser, User updatedUser) {
        if (StringUtils.hasText(updatedUser.getUsername())) {
            existingUser.setUsername(updatedUser.getUsername());
        }

        if (StringUtils.hasText(updatedUser.getEmail())) {
            existingUser.setEmail(updatedUser.getEmail());
        }

        if (StringUtils.hasText(updatedUser.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
    }

    private void updateAvatar(User user, MultipartFile avatarFile) {
        try {
            validateAvatarFile(avatarFile);
            String base64Avatar = imageService.convertToBase64(avatarFile);
            user.setAvatar(base64Avatar);
        } catch (IOException e) {
            log.error("Failed to update avatar for user: {}", user.getId(), e);
            throw new RuntimeException("Failed to update avatar", e);
        }
    }

    private void validateAvatarFile(MultipartFile file) {
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new IllegalArgumentException("Avatar file size must not exceed 1MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Invalid file type. Only image files are allowed");
        }
    }



}