package com.app.e_commerce.exception.invoice;

/**
 * Exception thrown when invoice export fails
 */
public class ExportException extends RuntimeException {
    
    public ExportException(String message) {
        super(message);
    }
    
    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static ExportException pdfGenerationFailed(String invoiceId, Throwable cause) {
        return new ExportException(
            String.format("Failed to generate PDF for invoice %s", invoiceId), 
            cause
        );
    }
    
    public static ExportException excelGenerationFailed(String invoiceId, Throwable cause) {
        return new ExportException(
            String.format("Failed to generate Excel for invoice %s", invoiceId), 
            cause
        );
    }
    
    public static ExportException csvGenerationFailed(String invoiceId, Throwable cause) {
        return new ExportException(
            String.format("Failed to generate CSV for invoice %s", invoiceId), 
            cause
        );
    }
}
