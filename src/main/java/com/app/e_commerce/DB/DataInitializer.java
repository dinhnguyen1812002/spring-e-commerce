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
     PasswordEncoder passwordEncoder;
    @Autowired
    UserRepo userRepo;
    @Bean
    public CommandLineRunner initData(RoleRepo roleRepository) {
        return args -> {
            List<String> roles = Arrays.asList("USER", "ADMIN");

            for (String roleName : roles) {
                if (roleRepository.findByName(roleName).isEmpty()) {
                    Role role = new Role();
                    role.setName(roleName);
                    roleRepository.save(role);
                }
            }

            // ✅ Kiểm tra xem user admin đã tồn tại chưa
            if (userRepo.findByEmail("admin@example.com").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setRoles(Collections.singleton(roleRepository.findByName("ADMIN").get()));
                admin.setPassword(passwordEncoder.encode("admin"));
                userRepo.save(admin);
                System.out.println("Admin user created.");
            } else {
                System.out.println("Admin user already exists. Skipping seed.");
            }
        };
    }

}
