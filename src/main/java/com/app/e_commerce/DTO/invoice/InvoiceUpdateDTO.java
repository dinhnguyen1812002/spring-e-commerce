package com.app.e_commerce.DTO.invoice;

import com.app.e_commerce.Enum.InvoiceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for updating existing invoices
 * Only editable fields are included
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceUpdateDTO {

    private LocalDateTime dueDate;

    // Customer information (editable in DRAFT status)
    @Size(max = 255, message = "Customer name must not exceed 255 characters")
    private String customerName;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Customer email must not exceed 255 characters")
    private String customerEmail;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String customerPhone;

    @Size(max = 1000, message = "Billing address must not exceed 1000 characters")
    private String billingAddress;

    @Size(max = 1000, message = "Shipping address must not exceed 1000 characters")
    private String shippingAddress;

    // Payment information
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;

    @Size(max = 255, message = "Payment transaction ID must not exceed 255 characters")
    private String paymentTransactionId;

    private LocalDateTime paidDate;

    // Additional fields
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    @Size(max = 2000, message = "Terms must not exceed 2000 characters")
    private String terms;

    @Size(max = 50, message = "Coupon code must not exceed 50 characters")
    private String couponCode;

    // Financial fields (editable in DRAFT status)
    @DecimalMin(value = "0.00", message = "Subtotal must be positive")
    private BigDecimal subtotal;

    @DecimalMin(value = "0.00", message = "Tax amount must be positive")
    private BigDecimal taxAmount;

    @DecimalMin(value = "0.00", message = "Discount amount must be positive")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.00", message = "Shipping cost must be positive")
    private BigDecimal shippingCost;

    // Invoice items (editable in DRAFT status)
    @Valid
    private List<InvoiceItemDTO> items;

    // Status change
    private InvoiceStatus status;

    // Reason for change (for audit trail)
    @Size(max = 500, message = "Change reason must not exceed 500 characters")
    private String changeReason;
}
