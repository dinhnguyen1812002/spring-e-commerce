package com.app.e_commerce.service.impl;

import com.app.e_commerce.Enum.InvoiceFormat;
import com.app.e_commerce.entity.Invoice;
import com.app.e_commerce.entity.InvoiceItem;
import com.app.e_commerce.exception.invoice.InvoiceNotFoundException;
import com.app.e_commerce.repository.InvoiceRepository;
import com.app.e_commerce.service.InvoiceExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceExportServiceImpl implements InvoiceExportService {

    private final InvoiceRepository invoiceRepository;
    private final TemplateEngine templateEngine;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] exportToPdf(String invoiceId) {
        log.info("Exporting invoice {} to PDF", invoiceId);
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> InvoiceNotFoundException.byId(invoiceId));
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Context context = new Context();
            context.setVariable("invoice", invoice);
            
            String htmlContent = templateEngine.process("invoices/pdf_export", context);
            
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(baos);
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF for invoice {}", invoiceId, e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    @Override
    public byte[] exportToExcel(String invoiceId) {
        log.info("Exporting invoice {} to Excel", invoiceId);
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> InvoiceNotFoundException.byId(invoiceId));
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Hóa đơn");
            
            // Styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle boldStyle = createBoldStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("HÓA ĐƠN #" + invoice.getInvoiceNumber());
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
            
            rowNum++; // Empty row
            
            // Invoice info
            createInfoRow(sheet, rowNum++, "Ngày tạo:", invoice.getInvoiceDate().format(DATE_FORMATTER), boldStyle);
            createInfoRow(sheet, rowNum++, "Trạng thái:", invoice.getStatus().toString(), boldStyle);
            createInfoRow(sheet, rowNum++, "Khách hàng:", invoice.getCustomerName(), boldStyle);
            createInfoRow(sheet, rowNum++, "Email:", invoice.getCustomerEmail(), boldStyle);
            createInfoRow(sheet, rowNum++, "Điện thoại:", invoice.getCustomerPhone() != null ? invoice.getCustomerPhone() : "", boldStyle);
            createInfoRow(sheet, rowNum++, "Địa chỉ:", invoice.getShippingAddress() != null ? invoice.getShippingAddress() : "", boldStyle);
            
            rowNum++; // Empty row
            
            // Items header
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"STT", "Sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Items
            int itemNum = 1;
            for (InvoiceItem item : invoice.getItems()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(itemNum++);
                row.createCell(1).setCellValue(item.getProductName());
                row.createCell(2).setCellValue(item.getQuantity());
                
                Cell priceCell = row.createCell(3);
                priceCell.setCellValue(item.getUnitPrice().doubleValue());
                priceCell.setCellStyle(currencyStyle);
                
                Cell totalCell = row.createCell(4);
                totalCell.setCellValue(item.getLineTotal().doubleValue());
                totalCell.setCellStyle(currencyStyle);
            }
            
            rowNum++; // Empty row
            
            // Totals
            createTotalRow(sheet, rowNum++, "Tạm tính:", invoice.getSubtotal().doubleValue(), boldStyle, currencyStyle);
            createTotalRow(sheet, rowNum++, "Thuế:", invoice.getTaxAmount().doubleValue(), boldStyle, currencyStyle);
            createTotalRow(sheet, rowNum++, "Phí vận chuyển:", invoice.getShippingCost().doubleValue(), boldStyle, currencyStyle);
            if (invoice.getDiscountAmount().doubleValue() > 0) {
                createTotalRow(sheet, rowNum++, "Giảm giá:", -invoice.getDiscountAmount().doubleValue(), boldStyle, currencyStyle);
            }
            createTotalRow(sheet, rowNum++, "TỔNG CỘNG:", invoice.getTotalAmount().doubleValue(), boldStyle, currencyStyle);
            
            // Auto-size columns
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error generating Excel for invoice {}", invoiceId, e);
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }
    
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0 ₫"));
        return style;
    }
    
    private CellStyle createBoldStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
    
    private void createInfoRow(Sheet sheet, int rowNum, String label, String value, CellStyle labelStyle) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        row.createCell(1).setCellValue(value);
    }
    
    private void createTotalRow(Sheet sheet, int rowNum, String label, double value, CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(3);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        Cell valueCell = row.createCell(4);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(valueStyle);
    }

    @Override
    public byte[] exportToCsv(String invoiceId) {
        log.info("Exporting invoice {} to CSV", invoiceId);
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> InvoiceNotFoundException.byId(invoiceId));

        StringBuilder csv = new StringBuilder();
        csv.append("Item,Quantity,Unit Price,Total\n");
        
        for (InvoiceItem item : invoice.getItems()) {
            csv.append(escapeCsv(item.getProductName())).append(",")
               .append(item.getQuantity()).append(",")
               .append(item.getUnitPrice()).append(",")
               .append(item.getLineTotal()).append("\n");
        }
        
        csv.append("\n,,Subtotal,").append(invoice.getSubtotal()).append("\n");
        csv.append(",,Tax,").append(invoice.getTaxAmount()).append("\n");
        csv.append(",,Total,").append(invoice.getTotalAmount()).append("\n");
        
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] bulkExport(List<String> invoiceIds, InvoiceFormat format, boolean combineIntoSingleFile) {
        // Simple mock implementation for bulk export
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (String id : invoiceIds) {
                byte[] content;
                String extension;
                switch (format) {
                    case PDF -> { content = exportToPdf(id); extension = "pdf"; }
                    case EXCEL -> { content = exportToExcel(id); extension = "xlsx"; }
                    case CSV -> { content = exportToCsv(id); extension = "csv"; }
                    default -> throw new IllegalArgumentException("Unsupported format");
                }
                
                ZipEntry entry = new ZipEntry("invoice_" + id + "." + extension);
                zos.putNextEntry(entry);
                zos.write(content);
                zos.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create zip archive", e);
        }
        return baos.toByteArray();
    }

    @Override
    public String getContentType(InvoiceFormat format) {
        return switch (format) {
            case PDF -> "application/pdf";
            case EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case CSV -> "text/csv";
        };
    }

    @Override
    public String getFileExtension(InvoiceFormat format) {
        return switch (format) {
            case PDF -> ".pdf";
            case EXCEL -> ".xlsx";
            case CSV -> ".csv";
        };
    }
    
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
