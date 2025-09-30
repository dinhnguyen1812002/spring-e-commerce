package com.app.e_commerce.services;

import com.app.e_commerce.DTO.ImportPreviewDTO;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class ExcelParsingService {
    
    private static final int PREVIEW_ROWS = 10;
    
    public List<String> getSheetNames(MultipartFile file) throws IOException {
        List<String> sheetNames = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = createWorkbook(inputStream, file.getOriginalFilename());
            
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheetNames.add(workbook.getSheetName(i));
            }
            
            workbook.close();
        }
        
        return sheetNames;
    }
    
    public ImportPreviewDTO parseFilePreview(MultipartFile file, String sheetName) throws IOException {
        ImportPreviewDTO preview = new ImportPreviewDTO();
        List<Map<String, Object>> sampleData = new ArrayList<>();
        List<String> columnHeaders = new ArrayList<>();
        Map<String, String> suggestedMappings = new HashMap<>();
        List<String> validationErrors = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = createWorkbook(inputStream, file.getOriginalFilename());
            Sheet sheet = workbook.getSheet(sheetName);
            
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet '" + sheetName + "' not found");
            }
            
            // Get header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("No header row found in sheet");
            }
            
            // Extract column headers
            for (Cell cell : headerRow) {
                String headerValue = getCellValueAsString(cell);
                columnHeaders.add(headerValue);
            }
            
            // Generate suggested mappings
            suggestedMappings = generateSuggestedMappings(columnHeaders);
            
            // Parse sample data rows
            int rowCount = 0;
            for (int i = 1; i <= Math.min(sheet.getLastRowNum(), PREVIEW_ROWS); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Map<String, Object> rowData = new HashMap<>();
                boolean hasData = false;
                
                for (int j = 0; j < columnHeaders.size(); j++) {
                    Cell cell = row.getCell(j);
                    Object cellValue = getCellValue(cell);
                    rowData.put(columnHeaders.get(j), cellValue);
                    if (cellValue != null && !cellValue.toString().trim().isEmpty()) {
                        hasData = true;
                    }
                }
                
                if (hasData) {
                    sampleData.add(rowData);
                    rowCount++;
                }
            }
            
            preview.setSampleData(sampleData);
            preview.setColumnHeaders(columnHeaders);
            preview.setSuggestedMappings(suggestedMappings);
            preview.setTotalRows(sheet.getLastRowNum());
            preview.setValidationErrors(validationErrors);
            preview.setHasErrors(!validationErrors.isEmpty());
            
            workbook.close();
        }
        
        return preview;
    }
    
    public List<Map<String, Object>> parseFileData(MultipartFile file, String sheetName, 
                                                   Map<String, String> columnMappings) throws IOException {
        List<Map<String, Object>> data = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = createWorkbook(inputStream, file.getOriginalFilename());
            Sheet sheet = workbook.getSheet(sheetName);
            
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet '" + sheetName + "' not found");
            }
            
            // Get header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("No header row found in sheet");
            }
            
            // Create column index mapping
            Map<String, Integer> columnIndexMap = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                String headerValue = getCellValueAsString(cell);
                columnIndexMap.put(headerValue, i);
            }
            
            // Parse data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Map<String, Object> rowData = new HashMap<>();
                boolean hasData = false;
                
                for (Map.Entry<String, String> mapping : columnMappings.entrySet()) {
                    String excelColumn = mapping.getKey();
                    String productField = mapping.getValue();
                    
                    Integer columnIndex = columnIndexMap.get(excelColumn);
                    if (columnIndex != null) {
                        Cell cell = row.getCell(columnIndex);
                        Object cellValue = getCellValue(cell);
                        rowData.put(productField, cellValue);
                        
                        if (cellValue != null && !cellValue.toString().trim().isEmpty()) {
                            hasData = true;
                        }
                    }
                }
                
                if (hasData) {
                    data.add(rowData);
                }
            }
            
            workbook.close();
        }
        
        return data;
    }
    
    private Workbook createWorkbook(InputStream inputStream, String fileName) throws IOException {
        String fileExtension = getFileExtension(fileName);
        
        switch (fileExtension.toLowerCase()) {
            case "xlsx":
                return new XSSFWorkbook(inputStream);
            case "xls":
                return new HSSFWorkbook(inputStream);
            default:
                throw new IllegalArgumentException("Unsupported file format: " + fileExtension);
        }
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
    
    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    private String getCellValueAsString(Cell cell) {
        Object value = getCellValue(cell);
        return value != null ? value.toString() : "";
    }
    
    private Map<String, String> generateSuggestedMappings(List<String> columnHeaders) {
        Map<String, String> mappings = new HashMap<>();
        
        // Common mapping patterns
        Map<String, String> commonMappings = new HashMap<>();
        commonMappings.put("name", "name");
        commonMappings.put("product name", "name");
        commonMappings.put("title", "name");
        commonMappings.put("product title", "name");
        commonMappings.put("price", "price");
        commonMappings.put("cost", "price");
        commonMappings.put("amount", "price");
        commonMappings.put("description", "description");
        commonMappings.put("desc", "description");
        commonMappings.put("details", "description");
        commonMappings.put("category", "category");
        commonMappings.put("cat", "category");
        commonMappings.put("type", "category");
        commonMappings.put("stock", "stock");
        commonMappings.put("quantity", "stock");
        commonMappings.put("qty", "stock");
        commonMappings.put("image", "image");
        commonMappings.put("photo", "image");
        commonMappings.put("picture", "image");
        commonMappings.put("url", "image");
        
        for (String header : columnHeaders) {
            String lowerHeader = header.toLowerCase().trim();
            
            // Direct match
            if (commonMappings.containsKey(lowerHeader)) {
                mappings.put(header, commonMappings.get(lowerHeader));
                continue;
            }
            
            // Partial match
            for (Map.Entry<String, String> entry : commonMappings.entrySet()) {
                if (lowerHeader.contains(entry.getKey()) || entry.getKey().contains(lowerHeader)) {
                    mappings.put(header, entry.getValue());
                    break;
                }
            }
        }
        
        return mappings;
    }
}
