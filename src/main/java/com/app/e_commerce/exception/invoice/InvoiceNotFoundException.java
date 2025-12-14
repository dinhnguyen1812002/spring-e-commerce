package com.app.e_commerce.exception.invoice;

/**
 * Exception thrown when an invoice is not found
 */
public class InvoiceNotFoundException extends RuntimeException {
    
    public InvoiceNotFoundException(String message) {
        super(message);
    }
    
    public InvoiceNotFoundException(String invoiceId, String field) {
        super(String.format("Invoice not found with %s: %s", field, invoiceId));
    }
    
    public static InvoiceNotFoundException byId(String invoiceId) {
        return new InvoiceNotFoundException(invoiceId, "id");
    }
    
    public static InvoiceNotFoundException byInvoiceNumber(String invoiceNumber) {
        return new InvoiceNotFoundException(invoiceNumber, "invoice number");
    }
}
