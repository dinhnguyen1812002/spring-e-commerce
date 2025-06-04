package com.app.e_commerce.repository;

import com.app.e_commerce.entity.Cart;
import com.app.e_commerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUser(User user);
    Optional<Cart> findByUserAndIsGuestCartFalse(User user);
    Optional<Cart> findByIdAndIsGuestCartTrue(UUID cartId);
}
