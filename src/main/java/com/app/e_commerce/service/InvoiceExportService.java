package com.app.e_commerce.service;

import com.app.e_commerce.Enum.InvoiceFormat;
import com.app.e_commerce.entity.Invoice;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Service interface for invoice export operations
 */
public interface InvoiceExportService {

    /**
     * Export invoice to PDF format
     */
    byte[] exportToPdf(String invoiceId);

    /**
     * Export invoice to Excel format
     */
    byte[] exportToExcel(String invoiceId);

    /**
     * Export invoice to CSV format
     */
    byte[] exportToCsv(String invoiceId);

    /**
     * Export multiple invoices in bulk
     */
    byte[] bulkExport(List<String> invoiceIds, InvoiceFormat format, boolean combineIntoSingleFile);

    /**
     * Get content type for export format
     */
    String getContentType(InvoiceFormat format);

    /**
     * Get file extension for export format
     */
    String getFileExtension(InvoiceFormat format);
}
