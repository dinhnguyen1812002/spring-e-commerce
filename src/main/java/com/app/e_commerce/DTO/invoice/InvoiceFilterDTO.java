package com.app.e_commerce.DTO.invoice;

import com.app.e_commerce.Enum.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for filtering and searching invoices
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceFilterDTO {

    // Pagination
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "invoiceDate";
    private String sortDirection = "DESC"; // ASC or DESC

    // Filters
    private String userId; // Filter by user (admin can filter by any user)
    private InvoiceStatus status;
    private String invoiceNumber;
    private String customerName;
    private String customerEmail;

    // Date range
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    // Amount range
    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    // Payment filters
    private String paymentMethod;
    private Boolean isPaid; // Filter by whether invoice is paid

    // Search query (searches across multiple fields)
    private String searchQuery;

    // Include archived/cancelled
    private Boolean includeArchived = false;
}
