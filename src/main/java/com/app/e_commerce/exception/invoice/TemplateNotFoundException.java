package com.app.e_commerce.exception.invoice;

/**
 * Exception thrown when an invoice template is not found
 */
public class TemplateNotFoundException extends RuntimeException {
    
    public TemplateNotFoundException(String message) {
        super(message);
    }
    
    public TemplateNotFoundException(Long templateId) {
        super(String.format("Invoice template not found with id: %d", templateId));
    }
    
    public static TemplateNotFoundException noActiveTemplate() {
        return new TemplateNotFoundException("No active invoice template found");
    }
    
    public static TemplateNotFoundException noDefaultTemplate() {
        return new TemplateNotFoundException("No default invoice template found");
    }
}
