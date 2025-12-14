package com.app.e_commerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entity representing a line item in an invoice.
 * Contains product information, pricing, and calculations.
 */
@Entity
@Table(name = "invoice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false, length = 255)
    private String productName;

    @Column(length = 100)
    private String productSku;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO; // Percentage (e.g., 10.00 for 10%)

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;

    /**
     * Calculate line total based on quantity, unit price, tax, and discount
     */
    public void calculateLineTotal() {
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal taxAmount = subtotal.multiply(taxRate).divide(BigDecimal.valueOf(100));
        this.lineTotal = subtotal.add(taxAmount).subtract(discountAmount);
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        if (lineTotal == null || lineTotal.compareTo(BigDecimal.ZERO) == 0) {
            calculateLineTotal();
        }
    }
}
