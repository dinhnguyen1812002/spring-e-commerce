package com.app.e_commerce.controller.API;

import com.app.e_commerce.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
public class api {
    @Autowired
    UserRepo userRepo;

    @DeleteMapping("/deleteAll")
    public ResponseEntity<String> deleteAllUsers() {
        long count = userRepo.count();  // Get the count of all users before deletion
        if (count > 0) {
            userRepo.deleteAll();  // Deletes all users
            return ResponseEntity.ok("All users have been deleted. Total: " + count);
        } else {
            return ResponseEntity.ok("No users to delete.");
        }
    }
}
