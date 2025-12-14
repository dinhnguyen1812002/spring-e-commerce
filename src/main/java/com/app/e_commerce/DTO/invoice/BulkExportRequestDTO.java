package com.app.e_commerce.DTO.invoice;

import com.app.e_commerce.Enum.InvoiceFormat;
import com.app.e_commerce.Enum.InvoiceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for bulk export operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkExportRequestDTO {

    @NotNull(message = "Export format is required")
    private InvoiceFormat format;

    // Option 1: Export specific invoice IDs
    private List<String> invoiceIds = new ArrayList<>();

    // Option 2: Export by filters
    private InvoiceStatus status;
    private String userId;
    private String customerName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    // Export options
    private Boolean includeAuditTrail = false;
    private Boolean combineIntoSingleFile = true; // For PDF/Excel
    private String fileName; // Optional custom filename

    // Template selection
    private Long templateId; // Optional: use specific template for export
}
