package com.app.e_commerce.repository;

import com.app.e_commerce.Enum.OrderStatus;
import com.app.e_commerce.entity.Order;
import com.app.e_commerce.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByUser(User user);
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalAmountByOrderDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    Page<Order> findByUserOrderByOrderDateDesc(User user, Pageable pageable);
    List<Order> findByUserOrderByOrderDateDesc(User user);

    // Additional useful methods
    Page<Order> findByUserAndOrderStatus(User user, OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.orderDate BETWEEN :startDate AND :endDate")
    Page<Order> findByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // For dashboard statistics
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user = :user AND o.orderStatus = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") OrderStatus status);
    /**
     * Find order by ID and user using query method
     */
    Optional<Order> findByIdAndUser(String id, User user);

    /**
     * Alternative: Using custom query for more complex scenarios
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems " +  // Eager fetch order details
            "WHERE o.id = :orderId " +
            "AND o.user = :user")
    Optional<Order> findByIdAndUserWithDetails(
            @Param("orderId") Long orderId,
            @Param("user") User user
    );

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal findTotalRevenue(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderStatus = :status AND o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal findPaidRevenue(LocalDateTime startDate, LocalDateTime endDate, OrderStatus status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderStatus IN :statuses AND o.paymentTransactionId IS NULL AND o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal findUnpaidRevenue(LocalDateTime startDate, LocalDateTime endDate, List<OrderStatus> statuses);

    long countByOrderStatusAndOrderDateBetween(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.orderStatus = :status AND o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal findAverageOrderValue(LocalDateTime startDate, LocalDateTime endDate, OrderStatus status);


}