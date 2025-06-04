package com.app.e_commerce.services;


import com.app.e_commerce.Enum.OrderStatus;
import com.app.e_commerce.Enum.PaymentMethod;
import com.app.e_commerce.entity.*;
import com.app.e_commerce.exception.ResourceNotFoundException;
import com.app.e_commerce.repository.OrderRepository;
import com.app.e_commerce.repository.OrderItemRepository;
import com.app.e_commerce.repository.UserRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private UserRepo userRepository;
    public void createOrder(User user, String address, PaymentMethod paymentMethod, HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");

        if (cart != null && !cart.getCartItems().isEmpty()) {
            Order order = new Order();
            order.setUser(user);
            order.setShippingAddress(address);
            order.setPaymentMethod(paymentMethod);
            order.setOrderDate(LocalDateTime.now());
            order.setTotalAmount(cart.getTotalPrice());
            CartService.setStatus(cart, order);
            orderRepository.save(order); // Save order to database
        }
    }

    public Order findOrderById(String id) {
        return orderRepository.findById(id).orElse(null);
    }

    public void createOrder(Order order) {
        orderRepository.save(order); // Save the order object
    }
    public List<Order> getOrdersForUser(User user) {
        return orderRepository.findByUser(user);
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }
//    public List<Order> getOrdersByUser(User user) {
//        return orderRepository.findByUser(user);
//    }
    public Page<Order> getOrdersByUserPaginated(User user, Pageable pageable) {
        return orderRepository.findByUserOrderByOrderDateDesc(user, pageable);
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }
    public Page<Order> getOrdersByUserAndStatus(User user, OrderStatus status, Pageable pageable) {
        try {
            return orderRepository.findByUserAndOrderStatus(user, status, pageable);
        } catch (Exception e) {

            throw new RuntimeException("Failed to retrieve orders by status", e);
        }
    }

    public Page<Order> getOrdersByUserAndDateRange(
            User user,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        try {
            // Validate date range
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date must be before end date");
            }

            // If either date is null, set to a reasonable default
            LocalDateTime effectiveStartDate = startDate != null ? startDate
                    : LocalDateTime.now().minusYears(1); // Default to 1 year ago
            LocalDateTime effectiveEndDate = endDate != null ? endDate
                    : LocalDateTime.now(); // Default to current time

            return orderRepository.findByUserAndDateRange(
                    user,
                    effectiveStartDate,
                    effectiveEndDate,
                    pageable
            );
        } catch (Exception e) {

            throw new RuntimeException("Failed to retrieve orders by date range", e);
        }
    }
    public Optional<Order> findByIdAndUser(String orderId, User user) {
        try {


            // Validate input
            if (orderId == null || user == null) {
//                log.error("OrderId or User is null");
                throw new IllegalArgumentException("OrderId and User cannot be null");
            }

            return orderRepository.findByIdAndUser(orderId, user);

        } catch (IllegalArgumentException e) {
//            log.error("Invalid arguments while finding order: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
//            log.error("Error finding order by id and user: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve order", e);
        }
    }

    /**
     * Find an order by its ID and user, throw exception if not found
     * Use this when you expect the order to exist
     */
    public Order findByIdAndUserOrThrow(String orderId, User user) {
        return findByIdAndUser(orderId, user)
                .orElseThrow(() -> {
//                    log.error("Order not found with id: {} for user: {}", orderId, user.getUsername());
                    return new ResourceNotFoundException(
                            String.format("Order with ID %d not found for user %s", orderId, user.getUsername())
                    );
                });
    }

    /**
     * Secure way to check if an order belongs to a user
     */
    public boolean isOrderOwnedByUser(String orderId, User user) {
        return findByIdAndUser(orderId, user).isPresent();
    }

}
