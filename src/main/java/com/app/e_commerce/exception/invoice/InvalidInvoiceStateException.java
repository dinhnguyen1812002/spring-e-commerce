package com.app.e_commerce.exception.invoice;

import com.app.e_commerce.Enum.InvoiceStatus;

/**
 * Exception thrown when an invalid invoice state transition is attempted
 */
public class InvalidInvoiceStateException extends RuntimeException {
    
    public InvalidInvoiceStateException(String message) {
        super(message);
    }
    
    public InvalidInvoiceStateException(InvoiceStatus currentStatus, InvoiceStatus targetStatus) {
        super(String.format("Invalid state transition from %s to %s", currentStatus, targetStatus));
    }
    
    public InvalidInvoiceStateException(String invoiceId, String operation) {
        super(String.format("Cannot perform operation '%s' on invoice %s in current state", operation, invoiceId));
    }
}
