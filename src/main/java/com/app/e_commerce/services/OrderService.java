package com.app.e_commerce.services;

import com.app.e_commerce.Enum.OrderStatus;
import com.app.e_commerce.Enum.PaymentMethod;
import com.app.e_commerce.entity.*;
import com.app.e_commerce.exception.ResourceNotFoundException;
import com.app.e_commerce.repository.OrderItemRepository;
import com.app.e_commerce.repository.OrderRepository;
import com.app.e_commerce.repository.ProductRepo;
import com.app.e_commerce.repository.UserRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepo productRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private NotificationService notificationService;
    /**
     * API mới: tạo Order từ Order skeleton + Cart đã có (được CartService chuẩn bị).
     * Thực hiện:
     * - Validate tồn kho
     * - Trừ tồn (Optimistic Locking)
     * - Map CartItem -> OrderItem
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Order order, Cart cart) {
        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống, không thể tạo đơn hàng.");
        }

        // Xây order items từ cart, đồng thời trừ tồn
        Set<OrderItem> orderItems = new HashSet<>();

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy sản phẩm với ID: " + cartItem.getProduct().getId()));

            // Kiểm tra tồn kho
            Integer currentStock = product.getStock();
            if (currentStock == null || currentStock < cartItem.getQuantity()) {
                throw new IllegalStateException("Không đủ hàng cho sản phẩm: " + product.getName());
            }

            // Trừ tồn (optimistic locking qua @Version)
            product.setStock(currentStock - cartItem.getQuantity());
            try {
                productRepository.save(product);
            } catch (OptimisticLockingFailureException e) {
                throw new IllegalStateException(
                        "Kho vừa thay đổi cho sản phẩm: " + product.getName() + ". Vui lòng thử lại.", e);
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setOrder(order);
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        if (order.getOrderStatus() == null) {
            order.setOrderStatus(OrderStatus.PENDING);
        }
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }

        // Lưu order và orderItems (cascade nếu đã cấu hình; ở đây dùng save trước cho chắc)
        Order saved = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        // Send notification to admin about new order
        notificationService.notifyAdminAboutNewOrder(saved);

        return saved;
    }

    /**
     * API cũ (giữ lại để tương thích Controller hiện tại nếu có).
     * Lấy cart từ session qua CartService thay vì session attribute "cart" tùy tiện.
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(User user,
                             String name,
                             String phone,
                             String note,
                             String address,
                             PaymentMethod paymentMethod,
                             HttpSession session) {

        Cart cart = cartService.getOrCreateCart(user, session);
        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống, không thể tạo đơn hàng.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setName(name);
        order.setPhoneNumber(phone);
        order.setNote(note);
        order.setShippingAddress(address);
        order.setPaymentMethod(paymentMethod);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus(OrderStatus.PENDING);

        Order created = createOrder(order, cart);

        // Clear cart
        cartService.clearCart(session, cart);
        return created;
    }

    public Order findOrderById(String id) {
        return orderRepository.findById(id).orElse(null);
    }

    public List<Order> getOrdersForUser(User user) {
        return orderRepository.findByUser(user);
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

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
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date must be before end date");
            }

            LocalDateTime effectiveStartDate = startDate != null ? startDate
                    : LocalDateTime.now().minusYears(1);
            LocalDateTime effectiveEndDate = endDate != null ? endDate
                    : LocalDateTime.now();

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
        if (orderId == null || user == null) {
            throw new IllegalArgumentException("OrderId and User cannot be null");
        }
        return orderRepository.findByIdAndUser(orderId, user);
    }

    public Order findByIdAndUserOrThrow(String orderId, User user) {
        // Check if user is admin - admins can view any order
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getName()));
        
        if (isAdmin) {
            // Admin can access any order
            return orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("Order with ID %s not found", orderId)
                    ));
        }
        
        // Regular users can only access their own orders
        return findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Order with ID %s not found for user %s", orderId, user.getUsername())
                ));
    }

    /**
     * Get recent orders with pagination
     * @param limit maximum number of orders to return
     * @return list of recent orders, ordered by order date descending
     */
    public List<Order> getRecentOrders(int limit) {
        return orderRepository
                .findAllByOrderByOrderDateDesc(PageRequest.of(0, limit))
                .getContent();
    }



    public boolean isOrderOwnedByUser(String orderId, User user) {
        return findByIdAndUser(orderId, user).isPresent();
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Update order status
     * @param orderId The order ID
     * @param status The new status
     * @return The updated order
     */
    @Transactional
    public Order updateOrderStatus(String orderId, OrderStatus status) {
        Order order = findOrderById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }

        // Store previous status for notification
        OrderStatus previousStatus = order.getOrderStatus();

        // Update status
        order.setOrderStatus(status);

        // Update date fields based on status
        if (status == OrderStatus.SHIPPED) {
            order.setShippedDate(LocalDateTime.now());
        } else if (status == OrderStatus.DELIVERED) {
            order.setDeliveredDate(LocalDateTime.now());
        }

        // Save the order
        Order updatedOrder = orderRepository.save(order);

        // Send notification to user about status change
        notificationService.notifyUserAboutOrderStatusChange(updatedOrder, previousStatus);

        return updatedOrder;
    }

    /**
     * Set tracking number for an order
     * @param orderId The order ID
     * @param trackingNumber The tracking number
     * @return The updated order
     */
    @Transactional
    public Order setTrackingNumber(String orderId, String trackingNumber) {
        Order order = findOrderById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }

        order.setTrackingNumber(trackingNumber);

        return orderRepository.save(order);
    }

    /**
     * Set payment transaction ID for an order
     * @param orderId The order ID
     * @param transactionId The payment transaction ID
     * @return The updated order
     */
    @Transactional
    public Order setPaymentTransactionId(String orderId, String transactionId) {
        Order order = findOrderById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }

        order.setPaymentTransactionId(transactionId);

        return orderRepository.save(order);
    }

    /**
     * Cancel an order
     * @param orderId The order ID
     * @param user The user requesting cancellation
     * @return The cancelled order
     */
    @Transactional
    public Order cancelOrder(String orderId, User user) {
        Order order = findByIdAndUserOrThrow(orderId, user);

        // Only allow cancellation of pending orders
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel order that is not in PENDING status");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);

        return orderRepository.save(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public Order checkoutCart(User user,
                              String name,
                              String phone,
                              String note,
                              String address,
                              PaymentMethod paymentMethod,
                              HttpSession session) {
        Cart cart = cartService.getOrCreateCart(user, session);

        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống, không thể tạo đơn hàng.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setName(name);
        order.setPhoneNumber(phone);
        order.setNote(note);
        order.setShippingAddress(address);
        order.setPaymentMethod(paymentMethod);
        order.setOrderDate(LocalDateTime.now());

        // Set pricing details from cart
        order.setSubtotal(cart.getSubtotal());
        order.setTax(cart.getTax());
        order.setShippingCost(cart.getShippingCost());
        order.setTotalAmount(cart.getTotalPrice());

        // Apply coupon if present
        if (cart.hasCoupon()) {
            order.setCouponCode(cart.getCouponCode());
            order.setDiscountAmount(cart.getDiscountAmount());
        }

        order.setOrderStatus(OrderStatus.PENDING);

        Set<OrderItem> orderItems = new HashSet<>();
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm: " + cartItem.getProduct().getId()));

            if (product.getStock() == null || product.getStock() < cartItem.getQuantity()) {
                throw new IllegalStateException("Không đủ hàng cho sản phẩm: " + product.getName());
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setOrder(order);
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        orderRepository.save(order);

        // clear cart sau khi tạo đơn
        cartService.clearCart(session);

        // Send notification to admin about new order
        notificationService.notifyAdminAboutNewOrder(order);

        return order;
    }


    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

}
