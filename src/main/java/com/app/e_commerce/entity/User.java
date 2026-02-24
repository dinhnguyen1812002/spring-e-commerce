package com.app.e_commerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean enabled = true;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    // Field for storing avatar image as binary data
    @Column(columnDefinition = "TEXT")
    private String avatar;
    @ManyToMany(fetch = FetchType.EAGER)

    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    // Getters and Setters
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Helper method to get the full data URI for the avatar.
     * Prevents SpEL concatenation limits for large base64 strings.
     */
    public String getAvatarUri() {
        if (avatar == null || avatar.isEmpty()) {
            return "/images/default-avatar.png";
        }
        if (avatar.startsWith("http") || avatar.startsWith("/")) {
            return avatar;
        }
        return "data:image/jpeg;base64," + avatar;
    }
}
