package com.app.e_commerce.controller;

import com.app.e_commerce.DTO.invoice.*;
import com.app.e_commerce.entity.User;
import com.app.e_commerce.services.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for invoice management operations
 */
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Slf4j
public class InvoiceRestController {

    private final InvoiceService invoiceService;
    private final com.app.e_commerce.repository.UserRepository userRepository;

    /**
     * Create a new invoice
     * POST /api/invoices
     */
    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(
            @Valid @RequestBody InvoiceRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Creating invoice for order: {}", requestDTO.getOrderId());
        User currentUser = getUserFromDetails(userDetails);
        InvoiceResponseDTO response = invoiceService.createInvoice(requestDTO, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get invoice by ID
     * GET /api/invoices/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoice(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting invoice: {}", id);
        User currentUser = getUserFromDetails(userDetails);
        InvoiceResponseDTO response = invoiceService.getInvoiceById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * List invoices with filters
     * GET /api/invoices
     */
    @GetMapping
    public ResponseEntity<Page<InvoiceResponseDTO>> listInvoices(
            @ModelAttribute InvoiceFilterDTO filterDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Listing invoices with filters");
        User currentUser = getUserFromDetails(userDetails);
        Page<InvoiceResponseDTO> response = invoiceService.listInvoices(filterDTO, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an invoice
     * PUT /api/invoices/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> updateInvoice(
            @PathVariable String id,
            @Valid @RequestBody InvoiceUpdateDTO updateDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Updating invoice: {}", id);
        User currentUser = getUserFromDetails(userDetails);
        InvoiceResponseDTO response = invoiceService.updateInvoice(id, updateDTO, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Finalize an invoice
     * POST /api/invoices/{id}/finalize
     */
    @PostMapping("/{id}/finalize")
    public ResponseEntity<InvoiceResponseDTO> finalizeInvoice(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Finalizing invoice: {}", id);
        User currentUser = getUserFromDetails(userDetails);
        InvoiceResponseDTO response = invoiceService.finalizeInvoice(id, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel an invoice
     * POST /api/invoices/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<InvoiceResponseDTO> cancelInvoice(
            @PathVariable String id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Cancelling invoice: {}", id);
        User currentUser = getUserFromDetails(userDetails);
        InvoiceResponseDTO response = invoiceService.cancelInvoice(id, reason, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an invoice
     * DELETE /api/invoices/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteInvoice(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Deleting invoice: {}", id);
        User currentUser = getUserFromDetails(userDetails);
        invoiceService.deleteInvoice(id, currentUser);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Invoice deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Get audit trail for an invoice
     * GET /api/invoices/{id}/audit-trail
     */
    @GetMapping("/{id}/audit-trail")
    public ResponseEntity<List<InvoiceAuditDTO>> getAuditTrail(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting audit trail for invoice: {}", id);
        User currentUser = getUserFromDetails(userDetails);
        List<InvoiceAuditDTO> response = invoiceService.getAuditTrail(id, currentUser);
        return ResponseEntity.ok(response);
    }

    // Helper method to get User from UserDetails
    private User getUserFromDetails(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));
    }
}
