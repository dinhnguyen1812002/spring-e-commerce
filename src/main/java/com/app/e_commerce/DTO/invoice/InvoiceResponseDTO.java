package com.app.e_commerce.DTO.invoice;

import com.app.e_commerce.Enum.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for invoice responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponseDTO {

    private String id;
    private String invoiceNumber;
    private InvoiceStatus status;
    
    // Order information
    private String orderId;
    
    // User information
    private String userId;
    private String username;
    
    // Template information
    private Long templateId;
    private String templateName;
    
    // Dates
    private LocalDateTime invoiceDate;
    private LocalDateTime dueDate;
    private LocalDateTime paidDate;
    
    // Financial fields
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;
    
    // Customer information
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String billingAddress;
    private String shippingAddress;
    
    // Payment information
    private String paymentMethod;
    private String paymentTransactionId;
    
    // Additional fields
    private String notes;
    private String terms;
    private String couponCode;
    
    // Invoice items
    private List<InvoiceItemDTO> items = new ArrayList<>();
    
    // Audit information
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String lastModifiedBy;
    
    // Computed fields
    private Integer totalItems;
    private Integer totalQuantity;
    private Boolean isEditable;
    private Boolean isFinalized;
}
