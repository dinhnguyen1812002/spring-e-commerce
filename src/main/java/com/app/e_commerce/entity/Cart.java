package com.app.e_commerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Column(nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal shippingCost = BigDecimal.valueOf(20000); // Default shipping cost

    @Column(nullable = true)
    private String couponCode;

    @Column(nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

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
        // Calculate subtotal from cart items
        this.subtotal = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate tax (10% of subtotal)
        this.tax = this.subtotal.multiply(BigDecimal.valueOf(0.1));

        // Calculate total price (subtotal + tax + shipping - discount)
        this.totalPrice = this.subtotal
                .add(this.tax)
                .add(this.shippingCost)
                .subtract(this.discountAmount);

        // Update last modified timestamp
        this.lastUpdated = LocalDateTime.now();
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

    /**
     * Apply a coupon code to the cart
     * @param code The coupon code
     * @param discountValue The discount amount to apply
     */
    public void applyCoupon(String code, BigDecimal discountValue) {
        if (code == null || code.trim().isEmpty()) {
            return;
        }

        this.couponCode = code;
        this.discountAmount = discountValue;
        updateTotalPrice();
    }

    /**
     * Remove the applied coupon from the cart
     */
    public void removeCoupon() {
        this.couponCode = null;
        this.discountAmount = BigDecimal.ZERO;
        updateTotalPrice();
    }

    /**
     * Check if the cart has a coupon applied
     * @return true if a coupon is applied, false otherwise
     */
    public boolean hasCoupon() {
        return this.couponCode != null && !this.couponCode.trim().isEmpty();
    }

    /**
     * Set custom shipping cost
     * @param cost The shipping cost
     */
    public void setShippingCost(BigDecimal cost) {
        if (cost != null && cost.compareTo(BigDecimal.ZERO) >= 0) {
            this.shippingCost = cost;
            updateTotalPrice();
        }
    }
}
