package com.app.e_commerce.entity;

import com.app.e_commerce.Enum.InvoiceStatus;
import de.huxhorn.sulky.ulid.ULID;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an invoice in the e-commerce system.
 * Invoices are generated from orders and can be exported in multiple formats.
 * Supports draft/pending/finalized states with audit trail tracking.
 */
@Entity
@Table(name = "invoices", indexes = {
    @Index(name = "idx_invoice_number", columnList = "invoiceNumber"),
    @Index(name = "idx_invoice_status", columnList = "status"),
    @Index(name = "idx_invoice_date", columnList = "invoiceDate"),
    @Index(name = "idx_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    private String id; // ULID for distributed system compatibility

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private InvoiceTemplate template;

    @Column(nullable = false, unique = true, length = 50)
    private String invoiceNumber; // Auto-generated: INV-2024-0001

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(nullable = false)
    private LocalDateTime invoiceDate;

    @Column(nullable = true)
    private LocalDateTime dueDate;

    // Financial fields
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Customer information (denormalized for historical accuracy)
    @Column(nullable = false, length = 255)
    private String customerName;

    @Column(nullable = false, length = 255)
    private String customerEmail;

    @Column(length = 20)
    private String customerPhone;

    @Column(columnDefinition = "TEXT")
    private String billingAddress;

    @Column(columnDefinition = "TEXT")
    private String shippingAddress;

    // Payment information
    @Column(length = 50)
    private String paymentMethod;

    @Column(length = 255)
    private String paymentTransactionId;

    @Column(nullable = true)
    private LocalDateTime paidDate;

    // Additional fields
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String terms;

    @Column(length = 50)
    private String couponCode;

    // Relationships
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceAudit> auditTrail = new ArrayList<>();

    // Audit fields
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false, updatable = false, length = 255)
    private String createdBy;

    @Column(nullable = false, length = 255)
    private String lastModifiedBy;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            ULID ulid = new ULID();
            this.id = ulid.nextULID();
        }
        if (invoiceDate == null) {
            invoiceDate = LocalDateTime.now();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Helper method to add an invoice item
     */
    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
    }

    /**
     * Helper method to remove an invoice item
     */
    public void removeItem(InvoiceItem item) {
        items.remove(item);
        item.setInvoice(null);
    }

    /**
     * Helper method to add an audit entry
     */
    public void addAuditEntry(InvoiceAudit audit) {
        auditTrail.add(audit);
        audit.setInvoice(this);
    }

    /**
     * Check if invoice can be edited
     */
    public boolean isEditable() {
        return status == InvoiceStatus.DRAFT || status == InvoiceStatus.PENDING;
    }

    /**
     * Check if invoice is finalized
     */
    public boolean isFinalized() {
        return status == InvoiceStatus.FINALIZED;
    }
}
