package com.app.e_commerce.services.impl;

import com.app.e_commerce.DTO.invoice.*;
import com.app.e_commerce.Enum.InvoiceStatus;
import com.app.e_commerce.entity.*;
import com.app.e_commerce.exception.invoice.*;
import com.app.e_commerce.repository.*;
import com.app.e_commerce.services.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of InvoiceService with business logic for invoice management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoiceAuditRepository auditRepository;
    private final InvoiceTemplateRepository templateRepository;
    private final com.app.e_commerce.repository.OrderRepository orderRepository;
    private final com.app.e_commerce.repository.UserRepository userRepository;

    private static final List<String> ADMIN_EDITABLE_FIELDS = List.of(
            "status", "dueDate", "notes", "terms", "paymentMethod",
            "paymentTransactionId", "paidDate");

    private static final List<String> USER_EDITABLE_FIELDS_DRAFT = List.of(
            "customerName", "customerEmail", "customerPhone",
            "billingAddress", "shippingAddress", "notes");

    @Override
    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO requestDTO, User currentUser) {
        log.info("Creating invoice for order: {} by user: {}", requestDTO.getOrderId(), currentUser.getUsername());

        // Validate order exists
        Order order = orderRepository.findById(requestDTO.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + requestDTO.getOrderId()));

        // Check if invoice already exists for this order
        if (invoiceRepository.existsByOrderId(order.getId())) {
            throw new RuntimeException("Invoice already exists for order: " + order.getId());
        }

        // Create invoice entity
        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setUser(order.getUser());
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setDueDate(
                requestDTO.getDueDate() != null ? requestDTO.getDueDate() : LocalDateTime.now().plusDays(30));

        // Set customer information from order or DTO
        invoice.setCustomerName(requestDTO.getCustomerName() != null ? requestDTO.getCustomerName() : order.getName());
        invoice.setCustomerEmail(
                requestDTO.getCustomerEmail() != null ? requestDTO.getCustomerEmail() : order.getUser().getEmail());
        invoice.setCustomerPhone(
                requestDTO.getCustomerPhone() != null ? requestDTO.getCustomerPhone() : order.getPhoneNumber());
        invoice.setBillingAddress(
                requestDTO.getBillingAddress() != null ? requestDTO.getBillingAddress() : order.getShippingAddress());
        invoice.setShippingAddress(
                requestDTO.getShippingAddress() != null ? requestDTO.getShippingAddress() : order.getShippingAddress());

        // Set financial information from order
        invoice.setSubtotal(order.getSubtotal());
        invoice.setTaxAmount(order.getTax());
        invoice.setDiscountAmount(order.getDiscountAmount());
        invoice.setShippingCost(order.getShippingCost());
        invoice.setTotalAmount(order.getTotalAmount());
        invoice.setCouponCode(order.getCouponCode());

        // Set payment information
        invoice.setPaymentMethod(order.getPaymentMethod().toString());
        invoice.setPaymentTransactionId(order.getPaymentTransactionId());

        // Set notes and terms
        invoice.setNotes(requestDTO.getNotes() != null ? requestDTO.getNotes() : order.getNote());
        invoice.setTerms(requestDTO.getTerms());

        // Set audit fields
        invoice.setCreatedBy(currentUser.getUsername());
        invoice.setLastModifiedBy(currentUser.getUsername());

        // Set template if specified
        if (requestDTO.getTemplateId() != null) {
            InvoiceTemplate template = templateRepository.findById(requestDTO.getTemplateId())
                    .orElseThrow(() -> new TemplateNotFoundException(requestDTO.getTemplateId()));
            invoice.setTemplate(template);
        }

        // Save invoice
        invoice = invoiceRepository.save(invoice);

        // Create invoice items from order items
        for (OrderItem orderItem : order.getOrderItems()) {
            InvoiceItem invoiceItem = new InvoiceItem();
            invoiceItem.setInvoice(invoice);
            invoiceItem.setProductName(orderItem.getProduct().getName());
            invoiceItem.setProductSku(orderItem.getProduct().getId().toString());
            invoiceItem.setQuantity(orderItem.getQuantity());
            invoiceItem.setUnitPrice(orderItem.getPrice());
            invoiceItem.setTaxRate(BigDecimal.ZERO); // Can be customized
            invoiceItem.setDiscountAmount(BigDecimal.ZERO);
            invoiceItem.calculateLineTotal();
            invoice.addItem(invoiceItem);
        }

        invoiceItemRepository.saveAll(invoice.getItems());

        // Create audit entry
        createAuditEntry(invoice, "CREATE", "Invoice created", currentUser.getUsername());

        log.info("Invoice created successfully: {}", invoice.getInvoiceNumber());
        return mapToResponseDTO(invoice);
    }

    @Override
    public InvoiceResponseDTO updateInvoice(String invoiceId, InvoiceUpdateDTO updateDTO, User currentUser) {
        log.info("Updating invoice: {} by user: {}", invoiceId, currentUser.getUsername());

        Invoice invoice = getInvoiceEntityById(invoiceId);

        // Check access
        if (!hasAccess(invoice, currentUser)) {
            throw new AccessDeniedException("You do not have permission to update this invoice");
        }

        // Check if invoice is editable
        if (invoice.getStatus() == InvoiceStatus.FINALIZED) {
            throw new InvoiceAlreadyFinalizedException(invoiceId);
        }

        // Update fields with audit trail
        updateFieldWithAudit(invoice, "customerName", invoice.getCustomerName(), updateDTO.getCustomerName(),
                currentUser);
        updateFieldWithAudit(invoice, "customerEmail", invoice.getCustomerEmail(), updateDTO.getCustomerEmail(),
                currentUser);
        updateFieldWithAudit(invoice, "customerPhone", invoice.getCustomerPhone(), updateDTO.getCustomerPhone(),
                currentUser);
        updateFieldWithAudit(invoice, "billingAddress", invoice.getBillingAddress(), updateDTO.getBillingAddress(),
                currentUser);
        updateFieldWithAudit(invoice, "shippingAddress", invoice.getShippingAddress(), updateDTO.getShippingAddress(),
                currentUser);
        updateFieldWithAudit(invoice, "notes", invoice.getNotes(), updateDTO.getNotes(), currentUser);
        updateFieldWithAudit(invoice, "terms", invoice.getTerms(), updateDTO.getTerms(), currentUser);

        // Update financial fields (only in DRAFT status)
        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            updateFieldWithAudit(invoice, "subtotal", invoice.getSubtotal(), updateDTO.getSubtotal(), currentUser);
            updateFieldWithAudit(invoice, "taxAmount", invoice.getTaxAmount(), updateDTO.getTaxAmount(), currentUser);
            updateFieldWithAudit(invoice, "discountAmount", invoice.getDiscountAmount(), updateDTO.getDiscountAmount(),
                    currentUser);
            updateFieldWithAudit(invoice, "shippingCost", invoice.getShippingCost(), updateDTO.getShippingCost(),
                    currentUser);

            // Recalculate total
            if (updateDTO.getSubtotal() != null || updateDTO.getTaxAmount() != null ||
                    updateDTO.getDiscountAmount() != null || updateDTO.getShippingCost() != null) {
                BigDecimal total = invoice.getSubtotal()
                        .add(invoice.getTaxAmount())
                        .add(invoice.getShippingCost())
                        .subtract(invoice.getDiscountAmount());
                invoice.setTotalAmount(total);
            }
        }

        invoice.setLastModifiedBy(currentUser.getUsername());
        invoice = invoiceRepository.save(invoice);

        log.info("Invoice updated successfully: {}", invoice.getInvoiceNumber());
        return mapToResponseDTO(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponseDTO getInvoiceById(String invoiceId, User currentUser) {
        Invoice invoice = getInvoiceEntityById(invoiceId);

        if (!hasAccess(invoice, currentUser)) {
            throw new AccessDeniedException("You do not have permission to view this invoice");
        }

        return mapToResponseDTO(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice getInvoiceEntityById(String invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> InvoiceNotFoundException.byId(invoiceId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponseDTO> listInvoices(InvoiceFilterDTO filterDTO, User currentUser) {
        log.info("Listing invoices with filters for user: {}", currentUser.getUsername());

        // Build pageable
        Sort sort = Sort.by(
                filterDTO.getSortDirection().equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
                filterDTO.getSortBy());
        Pageable pageable = PageRequest.of(filterDTO.getPage(), filterDTO.getSize(), sort);

        // Determine user filter (non-admin users can only see their own invoices)
        User userFilter = isAdmin(currentUser) && filterDTO.getUserId() != null
                ? userRepository.findById(java.util.UUID.fromString(filterDTO.getUserId())).orElse(null)
                : currentUser;

        // Apply filters
        Page<Invoice> invoices = invoiceRepository.findByFilters(
                userFilter,
                filterDTO.getStatus(),
                filterDTO.getStartDate(),
                filterDTO.getEndDate(),
                filterDTO.getCustomerName(),
                filterDTO.getMinAmount(),
                filterDTO.getMaxAmount(),
                pageable);

        return invoices.map(this::mapToResponseDTO);
    }

    @Override
    public InvoiceResponseDTO finalizeInvoice(String invoiceId, User currentUser) {
        log.info("Finalizing invoice: {} by user: {}", invoiceId, currentUser.getUsername());

        Invoice invoice = getInvoiceEntityById(invoiceId);

        if (!hasAccess(invoice, currentUser)) {
            throw new AccessDeniedException("You do not have permission to finalize this invoice");
        }

        if (invoice.getStatus() == InvoiceStatus.FINALIZED) {
            throw new InvalidInvoiceStateException("Invoice is already finalized");
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new InvalidInvoiceStateException("Cannot finalize a cancelled invoice");
        }

        invoice.setStatus(InvoiceStatus.FINALIZED);
        invoice.setLastModifiedBy(currentUser.getUsername());
        invoice = invoiceRepository.save(invoice);

        createAuditEntry(invoice, "STATUS_CHANGE", "Invoice finalized", currentUser.getUsername());

        log.info("Invoice finalized successfully: {}", invoice.getInvoiceNumber());
        return mapToResponseDTO(invoice);
    }

    @Override
    public InvoiceResponseDTO cancelInvoice(String invoiceId, String reason, User currentUser) {
        log.info("Cancelling invoice: {} by user: {}", invoiceId, currentUser.getUsername());

        Invoice invoice = getInvoiceEntityById(invoiceId);

        if (!hasAccess(invoice, currentUser)) {
            throw new AccessDeniedException("You do not have permission to cancel this invoice");
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new InvalidInvoiceStateException("Invoice is already cancelled");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setLastModifiedBy(currentUser.getUsername());
        invoice = invoiceRepository.save(invoice);

        InvoiceAudit audit = new InvoiceAudit();
        audit.setInvoice(invoice);
        audit.setFieldName("status");
        audit.setOldValue(InvoiceStatus.DRAFT.toString());
        audit.setNewValue(InvoiceStatus.CANCELLED.toString());
        audit.setChangedBy(currentUser.getUsername());
        audit.setChangeReason(reason);
        audit.setAction("STATUS_CHANGE");
        auditRepository.save(audit);

        log.info("Invoice cancelled successfully: {}", invoice.getInvoiceNumber());
        return mapToResponseDTO(invoice);
    }

    @Override
    public void deleteInvoice(String invoiceId, User currentUser) {
        log.info("Deleting invoice: {} by user: {}", invoiceId, currentUser.getUsername());

        Invoice invoice = getInvoiceEntityById(invoiceId);

        if (!isAdmin(currentUser)) {
            throw new AccessDeniedException("Only administrators can delete invoices");
        }

        if (invoice.getStatus() == InvoiceStatus.FINALIZED) {
            throw new InvalidInvoiceStateException("Cannot delete a finalized invoice");
        }

        invoiceRepository.delete(invoice);
        log.info("Invoice deleted successfully: {}", invoice.getInvoiceNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceAuditDTO> getAuditTrail(String invoiceId, User currentUser) {
        Invoice invoice = getInvoiceEntityById(invoiceId);

        if (!hasAccess(invoice, currentUser)) {
            throw new AccessDeniedException("You do not have permission to view this invoice's audit trail");
        }

        List<InvoiceAudit> audits = auditRepository.findByInvoiceIdOrderByChangedAtDesc(invoiceId);
        return audits.stream().map(this::mapToAuditDTO).collect(Collectors.toList());
    }

    @Override
    public boolean canEditField(String fieldName, Invoice invoice, User user) {
        // Admins can edit specific fields even when finalized
        if (isAdmin(user) && ADMIN_EDITABLE_FIELDS.contains(fieldName)) {
            return true;
        }

        // Regular users can only edit in DRAFT status
        if (invoice.getStatus() == InvoiceStatus.DRAFT && USER_EDITABLE_FIELDS_DRAFT.contains(fieldName)) {
            return true;
        }

        return false;
    }

    @Override
    public String generateInvoiceNumber() {
        String year = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy"));
        String latestNumber = invoiceRepository.findLatestInvoiceNumber().orElse("INV-" + year + "-0000");

        // Extract number and increment
        String[] parts = latestNumber.split("-");
        int number = parts.length >= 3 ? Integer.parseInt(parts[2]) + 1 : 1;

        return String.format("INV-%s-%04d", year, number);
    }

    @Override
    public boolean hasAccess(Invoice invoice, User user) {
        return invoice.getUser().getId().equals(user.getId()) || isAdmin(user);
    }

    // Helper methods

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN") || role.getName().equals("INVOICE_MANAGER"));
    }

    private void updateFieldWithAudit(Invoice invoice, String fieldName, Object oldValue, Object newValue, User user) {
        if (newValue == null || newValue.equals(oldValue)) {
            return;
        }

        // Update the field using reflection or switch statement
        switch (fieldName) {
            case "customerName" -> invoice.setCustomerName((String) newValue);
            case "customerEmail" -> invoice.setCustomerEmail((String) newValue);
            case "customerPhone" -> invoice.setCustomerPhone((String) newValue);
            case "billingAddress" -> invoice.setBillingAddress((String) newValue);
            case "shippingAddress" -> invoice.setShippingAddress((String) newValue);
            case "notes" -> invoice.setNotes((String) newValue);
            case "terms" -> invoice.setTerms((String) newValue);
            case "subtotal" -> invoice.setSubtotal((BigDecimal) newValue);
            case "taxAmount" -> invoice.setTaxAmount((BigDecimal) newValue);
            case "discountAmount" -> invoice.setDiscountAmount((BigDecimal) newValue);
            case "shippingCost" -> invoice.setShippingCost((BigDecimal) newValue);
        }

        // Create audit entry
        InvoiceAudit audit = new InvoiceAudit();
        audit.setInvoice(invoice);
        audit.setFieldName(fieldName);
        audit.setOldValue(oldValue != null ? oldValue.toString() : null);
        audit.setNewValue(newValue.toString());
        audit.setChangedBy(user.getUsername());
        audit.setAction("UPDATE");
        auditRepository.save(audit);
    }

    private void createAuditEntry(Invoice invoice, String action, String reason, String username) {
        InvoiceAudit audit = new InvoiceAudit();
        audit.setInvoice(invoice);
        audit.setAction(action);
        audit.setChangeReason(reason);
        audit.setChangedBy(username);
        audit.setFieldName("N/A");
        auditRepository.save(audit);
    }

    private InvoiceResponseDTO mapToResponseDTO(Invoice invoice) {
        InvoiceResponseDTO dto = new InvoiceResponseDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setStatus(invoice.getStatus());
        dto.setOrderId(invoice.getOrder().getId());
        dto.setUserId(invoice.getUser().getId().toString());
        dto.setUsername(invoice.getUser().getUsername());

        if (invoice.getTemplate() != null) {
            dto.setTemplateId(invoice.getTemplate().getId());
            dto.setTemplateName(invoice.getTemplate().getName());
        }

        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setPaidDate(invoice.getPaidDate());
        dto.setSubtotal(invoice.getSubtotal());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setDiscountAmount(invoice.getDiscountAmount());
        dto.setShippingCost(invoice.getShippingCost());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setCustomerName(invoice.getCustomerName());
        dto.setCustomerEmail(invoice.getCustomerEmail());
        dto.setCustomerPhone(invoice.getCustomerPhone());
        dto.setBillingAddress(invoice.getBillingAddress());
        dto.setShippingAddress(invoice.getShippingAddress());
        dto.setPaymentMethod(invoice.getPaymentMethod());
        dto.setPaymentTransactionId(invoice.getPaymentTransactionId());
        dto.setNotes(invoice.getNotes());
        dto.setTerms(invoice.getTerms());
        dto.setCouponCode(invoice.getCouponCode());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setUpdatedAt(invoice.getUpdatedAt());
        dto.setCreatedBy(invoice.getCreatedBy());
        dto.setLastModifiedBy(invoice.getLastModifiedBy());
        dto.setIsEditable(invoice.isEditable());
        dto.setIsFinalized(invoice.isFinalized());

        // Map items
        List<InvoiceItemDTO> itemDTOs = invoice.getItems().stream()
                .map(this::mapToItemDTO)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);
        dto.setTotalItems(itemDTOs.size());
        dto.setTotalQuantity(itemDTOs.stream().mapToInt(InvoiceItemDTO::getQuantity).sum());

        return dto;
    }

    private InvoiceItemDTO mapToItemDTO(InvoiceItem item) {
        InvoiceItemDTO dto = new InvoiceItemDTO();
        dto.setId(item.getId());
        dto.setProductName(item.getProductName());
        dto.setProductSku(item.getProductSku());
        dto.setDescription(item.getDescription());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTaxRate(item.getTaxRate());
        dto.setDiscountAmount(item.getDiscountAmount());
        dto.setLineTotal(item.getLineTotal());
        return dto;
    }

    private InvoiceAuditDTO mapToAuditDTO(InvoiceAudit audit) {
        InvoiceAuditDTO dto = new InvoiceAuditDTO();
        dto.setId(audit.getId());
        dto.setInvoiceId(audit.getInvoice().getId());
        dto.setFieldName(audit.getFieldName());
        dto.setOldValue(audit.getOldValue());
        dto.setNewValue(audit.getNewValue());
        dto.setChangedBy(audit.getChangedBy());
        dto.setChangedAt(audit.getChangedAt());
        dto.setChangeReason(audit.getChangeReason());
        dto.setAction(audit.getAction());
        return dto;
    }
}
