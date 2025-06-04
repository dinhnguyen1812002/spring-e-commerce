package com.app.e_commerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private boolean isGuestCart;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CartItem> cartItems = new HashSet<>();

    @Column(nullable = false)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    public void addCartItem(CartItem cartItem) {
        cartItems.add(cartItem);
        cartItem.setCart(this);
        updateTotalPrice();
    }

    public void removeCartItem(CartItem cartItem) {
        cartItems.remove(cartItem);
        cartItem.setCart(null);
        updateTotalPrice();
    }

    public void updateTotalPrice() {
        this.totalPrice = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void mergeCart(Cart otherCart) {
        for (CartItem otherItem : otherCart.getCartItems()) {
            CartItem existingItem = cartItems.stream()
                    .filter(item -> item.getProduct().getId().equals(otherItem.getProduct().getId()))
                    .findFirst()
                    .orElse(null);
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + otherItem.getQuantity());
            } else {
                CartItem newItem = new CartItem(otherItem.getProduct(), this, otherItem.getQuantity());
                cartItems.add(newItem);
            }
        }
        updateTotalPrice();
    }

    public boolean isGuestCart() {
        return isGuestCart;
    }

    public void setGuestCart(boolean guestCart) {
        isGuestCart = guestCart;
    }
}