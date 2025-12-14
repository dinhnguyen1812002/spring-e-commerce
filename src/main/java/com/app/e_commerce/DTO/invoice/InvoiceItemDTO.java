package com.app.e_commerce.DTO.invoice;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for invoice line items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemDTO {

    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String productName;

    @Size(max = 100, message = "Product SKU must not exceed 100 characters")
    private String productSku;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.00", message = "Unit price must be positive")
    @Digits(integer = 17, fraction = 2, message = "Unit price must have at most 2 decimal places")
    private BigDecimal unitPrice;

    @NotNull(message = "Tax rate is required")
    @DecimalMin(value = "0.00", message = "Tax rate must be positive")
    @DecimalMax(value = "100.00", message = "Tax rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Tax rate must have at most 2 decimal places")
    private BigDecimal taxRate = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Discount amount must be positive")
    @Digits(integer = 17, fraction = 2, message = "Discount amount must have at most 2 decimal places")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private BigDecimal lineTotal;
}
