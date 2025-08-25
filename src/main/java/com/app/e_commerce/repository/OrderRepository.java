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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing orders.
 */
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
     * Find the most recent orders with pagination
     * @param pageable pagination information
     * @return page of orders ordered by order date descending
     */
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);

    /**
     * Alternative: Using custom query for more complex scenarios
     */
    // Dashboard statistics queries
    @Query(value = "SELECT CAST(DATE(o.order_date) AS string) as day, SUM(o.total_amount) as revenue " +
            "FROM orders o " +
            "WHERE o.order_date >= CURRENT_DATE - INTERVAL '30 days' " +
            "GROUP BY DATE(o.order_date) " +
            "ORDER BY day", nativeQuery = true)
    List<Object[]> findDailyRevenueForLast30Days();

    @Query(value = "SELECT EXTRACT(MONTH FROM o.order_date) as month, SUM(o.total_amount) as revenue " +
            "FROM orders o " +
            "WHERE EXTRACT(YEAR FROM o.order_date) = EXTRACT(YEAR FROM CURRENT_DATE) " +
            "GROUP BY EXTRACT(MONTH FROM o.order_date) " +
            "ORDER BY month", nativeQuery = true)
    List<Object[]> findMonthlyRevenueForCurrentYear();

    @Query(value = "SELECT EXTRACT(QUARTER FROM o.order_date) as quarter, SUM(o.total_amount) as revenue " +
            "FROM orders o " +
            "WHERE EXTRACT(YEAR FROM o.order_date) = EXTRACT(YEAR FROM CURRENT_DATE) " +
            "GROUP BY EXTRACT(QUARTER FROM o.order_date) " +
            "ORDER BY quarter", nativeQuery = true)
    List<Object[]> findQuarterlyRevenueForCurrentYear();

    @Query(value = "SELECT EXTRACT(YEAR FROM o.order_date) as year, SUM(o.total_amount) as revenue " +
            "FROM orders o " +
            "WHERE o.order_date >= CURRENT_DATE - INTERVAL '5 years' " +
            "GROUP BY EXTRACT(YEAR FROM o.order_date) " +
            "ORDER BY year", nativeQuery = true)
    List<Object[]> findYearlyRevenueForLast5Years();

    @Query(value = "SELECT CAST(DATE(o.order_date) AS string) as day, COUNT(*) as sales " +
            "FROM orders o " +
            "WHERE o.order_date >= CURRENT_DATE - INTERVAL '30 days' " +
            "GROUP BY DATE(o.order_date) " +
            "ORDER BY day", nativeQuery = true)
    List<Object[]> findDailySalesForLast30Days();

    @Query(value = "SELECT EXTRACT(MONTH FROM o.order_date) as month, COUNT(*) as sales " +
            "FROM orders o " +
            "WHERE EXTRACT(YEAR FROM o.order_date) = EXTRACT(YEAR FROM CURRENT_DATE) " +
            "GROUP BY EXTRACT(MONTH FROM o.order_date) " +
            "ORDER BY month", nativeQuery = true)
    List<Object[]> findMonthlySalesForCurrentYear();

    @Query(value = "SELECT EXTRACT(QUARTER FROM o.order_date) as quarter, COUNT(*) as sales " +
            "FROM orders o " +
            "WHERE EXTRACT(YEAR FROM o.order_date) = EXTRACT(YEAR FROM CURRENT_DATE) " +
            "GROUP BY EXTRACT(QUARTER FROM o.order_date) " +
            "ORDER BY quarter", nativeQuery = true)
    List<Object[]> findQuarterlySalesForCurrentYear();

    @Query(value = "SELECT EXTRACT(YEAR FROM o.order_date) as year, COUNT(*) as sales " +
            "FROM orders o " +
            "WHERE o.order_date >= CURRENT_DATE - INTERVAL '5 years' " +
            "GROUP BY EXTRACT(YEAR FROM o.order_date) " +
            "ORDER BY year", nativeQuery = true)
    List<Object[]> findYearlySalesForLast5Years();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    BigDecimal getTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE EXTRACT(YEAR FROM o.orderDate) = :year AND EXTRACT(MONTH FROM o.orderDate) = :month")
    BigDecimal getMonthlyRevenue(@Param("year") int year, @Param("month") int month);

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