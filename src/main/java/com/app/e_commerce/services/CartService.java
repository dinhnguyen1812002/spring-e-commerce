package com.app.e_commerce.services;

import com.app.e_commerce.entity.*;
import com.app.e_commerce.Enum.OrderStatus;
import com.app.e_commerce.Enum.PaymentMethod;
import com.app.e_commerce.exception.ProductNotFoundException;
import com.app.e_commerce.repository.CartRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    private static final String GUEST_CART_ID = "guestCartId";

    @Transactional
    public Cart getOrCreateCart(User user, HttpSession session) {
        if (user != null) {
            // Authenticated user
            return cartRepository.findByUserAndIsGuestCartFalse(user)
                    .orElseGet(() -> {
                        Cart cart = new Cart();
                        cart.setUser(user);
//                        cart.setIsGuestCart(false);
                        cart.setGuestCart(false);
                        return cartRepository.save(cart);
                    });
        } else {
            // Guest user
            UUID guestCartId = (UUID) session.getAttribute(GUEST_CART_ID);
            if (guestCartId != null) {
                return cartRepository.findByIdAndIsGuestCartTrue(guestCartId)
                        .orElseGet(() -> createGuestCart(session));
            }
            return createGuestCart(session);
        }
    }

    private Cart createGuestCart(HttpSession session) {
        Cart cart = new Cart();
//        cart.setIsGuestCart(true);
        cart.setGuestCart(true);
        cart = cartRepository.save(cart);
        session.setAttribute(GUEST_CART_ID, cart.getId());
        return cart;
    }

    @Transactional
    public void mergeGuestCartToUserCart(User user, HttpSession session) {
        UUID guestCartId = (UUID) session.getAttribute(GUEST_CART_ID);
        if (guestCartId != null) {
            cartRepository.findByIdAndIsGuestCartTrue(guestCartId).ifPresent(guestCart -> {
                Cart userCart = getOrCreateCart(user, session);
                userCart.mergeCart(guestCart);
                cartRepository.save(userCart);
                cartRepository.delete(guestCart);
                session.removeAttribute(GUEST_CART_ID);
            });
        }
    }

    @Transactional
    public void addItemToCart(Product product, int quantity, User user, HttpSession session) {
        Cart cart = getOrCreateCart(user, session);
        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem cartItem = new CartItem(product, cart, quantity);
            cart.addCartItem(cartItem);
        }

        cart.updateTotalPrice();
        cartRepository.save(cart);
    }

    @Transactional
    public void removeItemFromCart(Long productId, User user, HttpSession session) {
        Cart cart = getOrCreateCart(user, session);
        cart.getCartItems().removeIf(item -> item.getProduct().getId().equals(productId));
        cart.updateTotalPrice();
        cartRepository.save(cart);
    }

    @Transactional
    public void updateCartItem(Long productId, int quantity, User user, HttpSession session) {
        Cart cart = getOrCreateCart(user, session);
        cart.getCartItems().forEach(item -> {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(quantity);
            }
        });
        cart.updateTotalPrice();
        cartRepository.save(cart);
    }

    @Transactional
    public void checkoutCart(String fullName, String phone, String address, String note, PaymentMethod paymentMethod, HttpSession session, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new IllegalStateException("User not found");
        }

        Cart cart = getOrCreateCart(user, session);
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setName(fullName);
        order.setPhoneNumber(phone);
        order.setShippingAddress(address);
        order.setNote(note);
        order.setPaymentMethod(paymentMethod);
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderDate(LocalDateTime.now());
        setStatus(cart, order);

        orderService.createOrder(order);
//        mailService.sendOrderConfirmationMail(user, order);

        cart.getCartItems().clear();
        cart.updateTotalPrice();
        cartRepository.save(cart);
        if (cart.isGuestCart()) {
            session.removeAttribute(GUEST_CART_ID);
        }
    }

    static void setStatus(Cart cart, Order order) {
        order.setOrderStatus(OrderStatus.PENDING);

        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }
    }
}