package com.app.e_commerce.exception.invoice;

/**
 * Exception thrown when attempting to edit a finalized invoice
 */
public class InvoiceAlreadyFinalizedException extends RuntimeException {
    
    public InvoiceAlreadyFinalizedException(String message) {
        super(message);
    }
    
//    public InvoiceAlreadyFinalizedException(String invoiceId) {
//        this(String.format("Invoice %s is already finalized and cannot be edited", invoiceId));
//    }
}
