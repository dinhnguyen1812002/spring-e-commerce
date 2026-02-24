package com.app.e_commerce.services;

import com.app.e_commerce.entity.Role;
import com.app.e_commerce.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class CustomUserDetails implements UserDetails {

    private UUID id;
    private String username;
    private String password;
    private String avatar; // Add avatar field here
    private Set<Role> roles;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.avatar = user.getAvatar(); // Set avatar here
        this.roles = user.getRoles();
    }

    public String getAvatarUri() {
        if (avatar == null || avatar.isEmpty()) {
            return "/images/default-avatar.png";
        }
        if (avatar.startsWith("http") || avatar.startsWith("/")) {
            return avatar;
        }
        return "data:image/jpeg;base64," + avatar;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
