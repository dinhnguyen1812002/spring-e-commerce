package com.app.e_commerce.DB;



import com.app.e_commerce.entity.Role;
import com.app.e_commerce.entity.User;
import com.app.e_commerce.repository.RoleRepo;

import com.app.e_commerce.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class DataInitializer {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepo userRepo;

    @Bean
    public CommandLineRunner initData(RoleRepo roleRepository) {
        return args -> {
            List<String> roles = Arrays.asList("USER", "ADMIN");

            // Seed roles
            for (String roleName : roles) {
                roleRepository.findByName(roleName).ifPresentOrElse(
                        role -> System.out.println("Role " + roleName + " already exists."),
                        () -> {
                            Role role = new Role();
                            role.setName(roleName);
                            roleRepository.save(role);
                            System.out.println("Role " + roleName + " created.");
                        }
                );
            }

            // Seed admin user (check cả email và username)
            boolean adminExists = userRepo.findByEmail("admin@example.com").isPresent()
                    || userRepo.findByUsername("admin").isPresent();

            if (adminExists) {
                System.out.println("Admin user already exists. Skipping seed.");
            } else {
                Role adminRole = roleRepository.findByName("ADMIN")
                        .orElseThrow(() -> new IllegalStateException("ADMIN role must exist"));

                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setEnabled(true);
                admin.setRoles(Collections.singleton(adminRole));
                userRepo.save(admin);
                System.out.println("Admin user created.");
            }
        };
    }
}
