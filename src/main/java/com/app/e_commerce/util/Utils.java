package com.app.e_commerce.util;

import com.app.e_commerce.entity.User;
import com.app.e_commerce.services.UserService;
import org.springframework.security.core.userdetails.UserDetails;

public class Utils {
    public static User getCurrentUser(UserDetails userDetails, UserService userService) {
        return userDetails != null ? userService.findByUsername(userDetails.getUsername()) : null;
    }
}
