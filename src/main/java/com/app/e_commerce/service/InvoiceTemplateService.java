package com.app.e_commerce.service;

import com.app.e_commerce.DTO.invoice.InvoiceTemplateDTO;
import com.app.e_commerce.entity.InvoiceTemplate;
import com.app.e_commerce.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for invoice template management
 */
public interface InvoiceTemplateService {

    /**
     * Create a new template
     */
    InvoiceTemplateDTO createTemplate(InvoiceTemplateDTO templateDTO, User currentUser);

    /**
     * Update an existing template
     */
    InvoiceTemplateDTO updateTemplate(Long id, InvoiceTemplateDTO templateDTO, User currentUser);

    /**
     * Get template by ID
     */
    InvoiceTemplateDTO getTemplateById(Long id);

    /**
     * Get template entity by ID (for internal use)
     */
    InvoiceTemplate getTemplateEntityById(Long id);

    /**
     * List all templates
     */
    Page<InvoiceTemplateDTO> listTemplates(Pageable pageable);

    /**
     * Activate a template
     */
    InvoiceTemplateDTO activateTemplate(Long id, User currentUser);

    /**
     * Deactivate a template
     */
    InvoiceTemplateDTO deactivateTemplate(Long id, User currentUser);

    /**
     * Delete a template
     */
    void deleteTemplate(Long id, User currentUser);

    /**
     * Get the currently active template
     */
    InvoiceTemplate getActiveTemplate();

    /**
     * Get the default template
     */
    InvoiceTemplate getDefaultTemplate();

    /**
     * Preview a template with sample data
     */
    String previewTemplate(Long id, String sampleInvoiceId);
}
