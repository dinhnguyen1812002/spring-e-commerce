package com.app.e_commerce.repository;

import com.app.e_commerce.Enum.InvoiceStatus;
import com.app.e_commerce.entity.Invoice;
import com.app.e_commerce.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Invoice entity.
 * Provides custom queries for filtering, searching, and bulk operations.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    /**
     * Find invoice by invoice number
     */
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    /**
     * Find all invoices for a specific user
     */
    Page<Invoice> findByUser(User user, Pageable pageable);

    /**
     * Find invoices by status
     */
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    /**
     * Find invoices by user and status
     */
    Page<Invoice> findByUserAndStatus(User user, InvoiceStatus status, Pageable pageable);

    /**
     * Find invoices within a date range
     */
    @Query("SELECT i FROM Invoice i WHERE i.invoiceDate BETWEEN :startDate AND :endDate")
    Page<Invoice> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Find invoices by user within a date range
     */
    @Query("SELECT i FROM Invoice i WHERE i.user = :user AND i.invoiceDate BETWEEN :startDate AND :endDate")
    Page<Invoice> findByUserAndDateRange(
        @Param("user") User user,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Find invoices by customer name (case-insensitive)
     */
    Page<Invoice> findByCustomerNameContainingIgnoreCase(String customerName, Pageable pageable);

    /**
     * Find invoices by customer email
     */
    Page<Invoice> findByCustomerEmailContainingIgnoreCase(String customerEmail, Pageable pageable);

    /**
     * Find invoices by total amount range
     */
    @Query("SELECT i FROM Invoice i WHERE i.totalAmount BETWEEN :minAmount AND :maxAmount")
    Page<Invoice> findByAmountRange(
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        Pageable pageable
    );

    /**
     * Complex filter query for invoices
     */
    @Query("SELECT i FROM Invoice i WHERE " +
           "(:user IS NULL OR i.user = :user) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:startDate IS NULL OR i.invoiceDate >= :startDate) AND " +
           "(:endDate IS NULL OR i.invoiceDate <= :endDate) AND " +
           "(:customerName IS NULL OR i.customerName LIKE CONCAT('%', :customerName, '%')) AND " +
           "(:minAmount IS NULL OR i.totalAmount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR i.totalAmount <= :maxAmount)")
    Page<Invoice> findByFilters(
        @Param("user") User user,
        @Param("status") InvoiceStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("customerName") String customerName,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        Pageable pageable
    );

    /**
     * Find invoices by multiple statuses
     */
    @Query("SELECT i FROM Invoice i WHERE i.status IN :statuses")
    List<Invoice> findByStatusIn(@Param("statuses") List<InvoiceStatus> statuses);

    /**
     * Count invoices by status
     */
    long countByStatus(InvoiceStatus status);

    /**
     * Count invoices by user
     */
    long countByUser(User user);

    /**
     * Get the latest invoice number for generating new invoice numbers
     */
    @Query("SELECT i.invoiceNumber FROM Invoice i ORDER BY i.createdAt DESC LIMIT 1")
    Optional<String> findLatestInvoiceNumber();

    /**
     * Find all invoices created by a specific user
     */
    Page<Invoice> findByCreatedBy(String createdBy, Pageable pageable);

    /**
     * Check if an invoice exists for a specific order
     */
    boolean existsByOrderId(String orderId);

    /**
     * Find invoice by order ID
     */
    Optional<Invoice> findByOrderId(String orderId);
}
