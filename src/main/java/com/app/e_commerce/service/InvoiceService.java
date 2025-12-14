package com.app.e_commerce.service;

import com.app.e_commerce.DTO.invoice.*;
import com.app.e_commerce.entity.Invoice;
import com.app.e_commerce.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for invoice management operations
 */
public interface InvoiceService {

    /**
     * Create a new invoice from an order
     */
    InvoiceResponseDTO createInvoice(InvoiceRequestDTO requestDTO, User currentUser);

    /**
     * Update an existing invoice
     */
    InvoiceResponseDTO updateInvoice(String invoiceId, InvoiceUpdateDTO updateDTO, User currentUser);

    /**
     * Get invoice by ID
     */
    InvoiceResponseDTO getInvoiceById(String invoiceId, User currentUser);

    /**
     * Get invoice entity by ID (for internal use)
     */
    Invoice getInvoiceEntityById(String invoiceId);

    /**
     * List invoices with filters and pagination
     */
    Page<InvoiceResponseDTO> listInvoices(InvoiceFilterDTO filterDTO, User currentUser);

    /**
     * Finalize an invoice (change status to FINALIZED)
     */
    InvoiceResponseDTO finalizeInvoice(String invoiceId, User currentUser);

    /**
     * Cancel an invoice
     */
    InvoiceResponseDTO cancelInvoice(String invoiceId, String reason, User currentUser);

    /**
     * Delete an invoice (soft delete)
     */
    void deleteInvoice(String invoiceId, User currentUser);

    /**
     * Get audit trail for an invoice
     */
    List<InvoiceAuditDTO> getAuditTrail(String invoiceId, User currentUser);

    /**
     * Validate if a user can edit a specific field
     */
    boolean canEditField(String fieldName, Invoice invoice, User user);

    /**
     * Generate invoice number
     */
    String generateInvoiceNumber();

    /**
     * Check if user owns the invoice or is admin
     */
    boolean hasAccess(Invoice invoice, User user);
}
