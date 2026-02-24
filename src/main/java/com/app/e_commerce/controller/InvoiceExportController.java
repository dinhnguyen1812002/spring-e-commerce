package com.app.e_commerce.controller;

import com.app.e_commerce.Enum.InvoiceFormat;
import com.app.e_commerce.DTO.invoice.InvoiceRequestDTO;
import com.app.e_commerce.DTO.invoice.InvoiceResponseDTO;
import com.app.e_commerce.services.InvoiceExportService;
import com.app.e_commerce.services.InvoiceService;
import com.app.e_commerce.repository.InvoiceRepository;
import com.app.e_commerce.repository.OrderRepository;
import com.app.e_commerce.entity.Invoice;
import com.app.e_commerce.entity.Order;
import com.app.e_commerce.exception.invoice.InvoiceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/invoices/export")
@RequiredArgsConstructor
@Slf4j
public class InvoiceExportController {

    private final InvoiceExportService invoiceExportService;
    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @GetMapping("/{id}/{format}")
    public ResponseEntity<Resource> exportInvoice(
            @PathVariable String id,
            @PathVariable String format) {
        
        log.info("Request to export invoice {} in format {}", id, format);
        
        InvoiceFormat invoiceFormat;
        try {
            invoiceFormat = InvoiceFormat.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid export format requested: {}", format);
            return ResponseEntity.badRequest().build();
        }

        try {
            byte[] data = switch (invoiceFormat) {
                case PDF -> invoiceExportService.exportToPdf(id);
                case EXCEL -> invoiceExportService.exportToExcel(id);
                case CSV -> invoiceExportService.exportToCsv(id);
            };

            Invoice invoice = invoiceRepository.findById(id).orElse(null);
            String invoiceNumber = invoice != null ? invoice.getInvoiceNumber() : id;
            String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
            String filename = "hoadon_" + invoiceNumber + "_" + timestamp + invoiceExportService.getFileExtension(invoiceFormat);
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename)
                    .contentType(MediaType.parseMediaType(invoiceExportService.getContentType(invoiceFormat)))
                    .contentLength(data.length)
                    .body(resource);
        } catch (InvoiceNotFoundException e) {
            log.error("Invoice not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error exporting invoice {} to {}", id, format, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    
    @GetMapping("/order/{orderId}/{format}")
    public ResponseEntity<Resource> exportInvoiceByOrder(
            @PathVariable String orderId,
            @PathVariable String format) {
        
        log.info("Request to export invoice for order {} in format {}", orderId, format);
        
        try {
            Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseGet(() -> createInvoiceForOrder(orderId));
                
            return exportInvoice(invoice.getId(), format);
        } catch (RuntimeException e) {
            log.error("Error exporting invoice for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private Invoice createInvoiceForOrder(String orderId) {
        log.info("Invoice not found for order {}, creating new one", orderId);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
            
        InvoiceRequestDTO requestDTO = new InvoiceRequestDTO();
        requestDTO.setOrderId(orderId);
        
        InvoiceResponseDTO responseDTO = invoiceService.createInvoice(requestDTO, order.getUser());
        
        return invoiceRepository.findById(responseDTO.getId())
            .orElseThrow(() -> InvoiceNotFoundException.byId(responseDTO.getId()));
    }
}
