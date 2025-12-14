package com.app.e_commerce.repository;

import com.app.e_commerce.entity.InvoiceAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for InvoiceAudit entity.
 */
@Repository
public interface InvoiceAuditRepository extends JpaRepository<InvoiceAudit, Long> {

    /**
     * Find all audit entries for a specific invoice
     */
    List<InvoiceAudit> findByInvoiceIdOrderByChangedAtDesc(String invoiceId);

    /**
     * Find audit entries for a specific invoice (paginated)
     */
    Page<InvoiceAudit> findByInvoiceId(String invoiceId, Pageable pageable);

    /**
     * Find audit entries by field name
     */
    List<InvoiceAudit> findByInvoiceIdAndFieldName(String invoiceId, String fieldName);

    /**
     * Find audit entries by user
     */
    List<InvoiceAudit> findByChangedBy(String changedBy);

    /**
     * Find audit entries within a date range
     */
    @Query("SELECT a FROM InvoiceAudit a WHERE a.changedAt BETWEEN :startDate AND :endDate ORDER BY a.changedAt DESC")
    List<InvoiceAudit> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find audit entries for a specific invoice and user
     */
    List<InvoiceAudit> findByInvoiceIdAndChangedByOrderByChangedAtDesc(String invoiceId, String changedBy);

    /**
     * Count audit entries for a specific invoice
     */
    long countByInvoiceId(String invoiceId);

    /**
     * Find audit entries by action type
     */
    List<InvoiceAudit> findByAction(String action);

    /**
     * Delete all audit entries for a specific invoice
     */
    void deleteByInvoiceId(String invoiceId);
}
