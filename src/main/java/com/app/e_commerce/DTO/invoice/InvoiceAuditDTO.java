package com.app.e_commerce.DTO.invoice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for invoice audit trail entries
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceAuditDTO {

    private Long id;
    private String invoiceId;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String changedBy;
    private LocalDateTime changedAt;
    private String changeReason;
    private String action;
}
