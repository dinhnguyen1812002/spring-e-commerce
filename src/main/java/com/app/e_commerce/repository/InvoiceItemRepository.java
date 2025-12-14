package com.app.e_commerce.repository;

import com.app.e_commerce.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for InvoiceItem entity.
 */
@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    /**
     * Find all items for a specific invoice
     */
    List<InvoiceItem> findByInvoiceId(String invoiceId);

    /**
     * Find items by product name
     */
    List<InvoiceItem> findByProductNameContainingIgnoreCase(String productName);

    /**
     * Find items by product SKU
     */
    List<InvoiceItem> findByProductSku(String productSku);

    /**
     * Delete all items for a specific invoice
     */
    void deleteByInvoiceId(String invoiceId);

    /**
     * Count items for a specific invoice
     */
    long countByInvoiceId(String invoiceId);

    /**
     * Get total quantity for a specific invoice
     */
    @Query("SELECT SUM(ii.quantity) FROM InvoiceItem ii WHERE ii.invoice.id = :invoiceId")
    Integer getTotalQuantityByInvoiceId(@Param("invoiceId") String invoiceId);
}
