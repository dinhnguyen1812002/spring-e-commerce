package com.app.e_commerce.DTO.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for creating new invoices
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequestDTO {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    private Long templateId; // Optional: specific template to use

    private LocalDateTime dueDate;

    // Customer information (optional, will be populated from order if not provided)
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

    // Invoice items (optional, will be populated from order if not provided)
    @Valid
    private List<InvoiceItemDTO> items = new ArrayList<>();

    // Additional fields
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    @Size(max = 2000, message = "Terms must not exceed 2000 characters")
    private String terms;

    // Financial overrides (optional)
    @DecimalMin(value = "0.00", message = "Subtotal must be positive")
    private BigDecimal subtotal;

    @DecimalMin(value = "0.00", message = "Tax amount must be positive")
    private BigDecimal taxAmount;

    @DecimalMin(value = "0.00", message = "Discount amount must be positive")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.00", message = "Shipping cost must be positive")
    private BigDecimal shippingCost;
}
