package com.app.e_commerce.services;
//
//import com.app.e_commerce.DTO.*;
//import com.app.e_commerce.entity.*;
//import com.app.e_commerce.repository.ImportErrorRepository;
//import com.app.e_commerce.repository.ProductImportRepository;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.concurrent.CompletableFuture;
//
//@Service
//public class ProductImportService {
//
//    @Autowired
//    private ProductImportRepository productImportRepository;
//
//    @Autowired
//    private ImportErrorRepository importErrorRepository;
//
//    @Autowired
//    private ExcelParsingService excelParsingService;
//
//    @Autowired
//    private GoogleSheetsService googleSheetsService;
//
//    @Autowired
//    private ProductImportValidationService validationService;
//
//    @Autowired
//    private ProductService productService;
//
//    @Autowired
//    private CategoryService categoryService;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public List<String> getAvailableSheets(MultipartFile file, String fileType) throws IOException {
//        if ("GOOGLE_SHEETS".equals(fileType)) {
//            // This would need the spreadsheet ID from the request
//            throw new UnsupportedOperationException("Google Sheets integration requires spreadsheet ID");
//        } else {
//            return excelParsingService.getSheetNames(file);
//        }
//    }
//
//    public ImportPreviewDTO previewImport(MultipartFile file, String fileType, String sheetName) throws IOException {
//        if ("GOOGLE_SHEETS".equals(fileType)) {
//            // This would need the spreadsheet ID from the request
//            throw new UnsupportedOperationException("Google Sheets integration requires spreadsheet ID");
//        } else {
//            return excelParsingService.parseFilePreview(file, sheetName);
//        }
//    }
//
//    public ImportPreviewDTO previewGoogleSheetsImport(String spreadsheetId, String sheetName) throws Exception {
//        return googleSheetsService.parseSheetPreview(spreadsheetId, sheetName);
//    }
//
//    public ColumnMappingDTO getColumnMappingInfo(List<String> availableColumns) {
//        return validationService.getColumnMappingInfo(availableColumns);
//    }
//
//    @Transactional
//    public ProductImportResponseDTO startImport(ProductImportRequestDTO request) throws JsonProcessingException {
//        ProductImport productImport = new ProductImport();
//        productImport.setFileName(request.getFileName());
//        productImport.setOriginalFileName(request.getOriginalFileName());
//        productImport.setStatus(ProductImport.ImportStatus.PENDING);
//        productImport.setMappingConfiguration(objectMapper.writeValueAsString(request.getColumnMappings()));
//
//        ProductImport savedImport = productImportRepository.save(productImport);
//
//        // Start async import process
//        processImportAsync(savedImport.getId(), request);
//
//        return new ProductImportResponseDTO(savedImport);
//    }
//
//    @Async
//    public CompletableFuture<Void> processImportAsync(Long importId, ProductImportRequestDTO request) {
//        try {
//            processImport(importId, request);
//        } catch (Exception e) {
//            updateImportStatus(importId, ProductImport.ImportStatus.FAILED, e.getMessage());
//        }
//        return CompletableFuture.completedFuture(null);
//    }
//
//    @Transactional
//    public void processImport(Long importId, ProductImportRequestDTO request) throws Exception {
//        ProductImport productImport = productImportRepository.findById(importId)
//                .orElseThrow(() -> new RuntimeException("Import not found"));
//
//        updateImportStatus(importId, ProductImport.ImportStatus.PROCESSING, null);
//
//        try {
//            List<Map<String, Object>> data;
//
//            if ("GOOGLE_SHEETS".equals(request.getFileType())) {
//                data = googleSheetsService.parseSheetData(
//                    request.getGoogleSheetsId(),
//                    request.getSheetName(),
//                    request.getColumnMappings()
//                );
//            } else {
//                // This would need the file to be stored temporarily
//                throw new UnsupportedOperationException("File-based import requires file storage implementation");
//            }
//
//            productImport.setTotalRows(data.size());
//            productImportRepository.save(productImport);
//
//            processBatchImport(importId, data, request);
//
//            updateImportStatus(importId, ProductImport.ImportStatus.COMPLETED, null);
//
//        } catch (Exception e) {
//            updateImportStatus(importId, ProductImport.ImportStatus.FAILED, e.getMessage());
//            throw e;
//        }
//    }
//
//    private void processBatchImport(Long importId, List<Map<String, Object>> data,
//                                  ProductImportRequestDTO request) {
//        int batchSize = request.getBatchSize() != null ? request.getBatchSize() : 100;
//        int successfulImports = 0;
//        int failedImports = 0;
//
//        for (int i = 0; i < data.size(); i += batchSize) {
//            int endIndex = Math.min(i + batchSize, data.size());
//            List<Map<String, Object>> batch = data.subList(i, endIndex);
//
//            for (int j = 0; j < batch.size(); j++) {
//                Map<String, Object> rowData = batch.get(j);
//                int rowNumber = i + j + 2; // +2 because Excel rows start from 1 and we skip header
//
//                try {
//                    // Validate row data
//                    List<ImportError> errors = validationService.validateRowData(
//                        rowData, rowNumber, request.getColumnMappings()
//                    );
//
//                    if (!errors.isEmpty()) {
//                        // Save validation errors
//                        for (ImportError error : errors) {
//                            error.setProductImport(productImportRepository.findById(importId).orElse(null));
//                            importErrorRepository.save(error);
//                        }
//                        failedImports++;
//                    } else {
//                        // Convert and save product
//                        Map<String, Object> convertedData = validationService.convertRowData(
//                            rowData, request.getColumnMappings()
//                        );
//
//                        Product product = createProductFromData(convertedData, request);
//                        productService.saveProduct(product, null); // No file upload for import
//                        successfulImports++;
//                    }
//
//                } catch (Exception e) {
//                    // Save error
//                    ImportError error = new ImportError();
//                    error.setProductImport(productImportRepository.findById(importId).orElse(null));
//                    error.setRowNumber(rowNumber);
//                    error.setErrorMessage("Import failed: " + e.getMessage());
//                    error.setErrorType(ImportError.ErrorType.VALIDATION_ERROR);
//                    importErrorRepository.save(error);
//                    failedImports++;
//                }
//
//                // Update progress
//                updateImportProgress(importId, i + j + 1, successfulImports, failedImports);
//            }
//        }
//    }
//
//    private Product createProductFromData(Map<String, Object> data, ProductImportRequestDTO request) {
//        Product product = new Product();
//
//        if (data.containsKey("name")) {
//            product.setName(data.get("name").toString());
//        }
//
//        if (data.containsKey("price")) {
//            product.setPrice((BigDecimal) data.get("price"));
//        }
//
//        if (data.containsKey("description")) {
//            product.setDescription(data.get("description").toString());
//        }
//
//        if (data.containsKey("stock")) {
//            product.setStock((Integer) data.get("stock"));
//        }
//
//        if (data.containsKey("image")) {
//            product.setImage(data.get("image").toString());
//        }
//
//        if (data.containsKey("category")) {
//            String categoryName = data.get("category").toString();
//            Category category = categoryService.findOrCreateCategoryByName(categoryName);
//            product.getCategories().add(category);
//        }
//
//        return product;
//    }
//
//    private void updateImportStatus(Long importId, ProductImport.ImportStatus status, String errorMessage) {
//        ProductImport productImport = productImportRepository.findById(importId).orElse(null);
//        if (productImport != null) {
//            productImport.setStatus(status);
//            if (errorMessage != null) {
//                productImport.setErrorMessage(errorMessage);
//            }
//            if (status == ProductImport.ImportStatus.COMPLETED || status == ProductImport.ImportStatus.FAILED) {
//                productImport.setCompletedAt(LocalDateTime.now());
//            }
//            productImportRepository.save(productImport);
//        }
//    }
//
//    private void updateImportProgress(Long importId, int processedRows, int successfulImports, int failedImports) {
//        ProductImport productImport = productImportRepository.findById(importId).orElse(null);
//        if (productImport != null) {
//            productImport.setProcessedRows(processedRows);
//            productImport.setSuccessfulImports(successfulImports);
//            productImport.setFailedImports(failedImports);
//            productImportRepository.save(productImport);
//        }
//    }
//
//    public ProductImportResponseDTO getImportStatus(Long importId) {
//        ProductImport productImport = productImportRepository.findById(importId)
//                .orElseThrow(() -> new RuntimeException("Import not found"));
//
//        ProductImportResponseDTO response = new ProductImportResponseDTO(productImport);
//
//        // Add errors if any
//        List<ImportError> errors = importErrorRepository.findByProductImportIdOrderByRowNumber(importId);
//        List<ImportErrorDTO> errorDTOs = errors.stream()
//                .map(ImportErrorDTO::new)
//                .toList();
//        response.setErrors(errorDTOs);
//
//        return response;
//    }
//
//    public List<ProductImportResponseDTO> getAllImports() {
//        List<ProductImport> imports = productImportRepository.findAllByOrderByStartedAtDesc(
//            org.springframework.data.domain.PageRequest.of(0, 50)
//        ).getContent();
//
//        return imports.stream()
//                .map(ProductImportResponseDTO::new)
//                .toList();
//    }
//
//    public void cancelImport(Long importId) {
//        updateImportStatus(importId, ProductImport.ImportStatus.CANCELLED, "Import cancelled by user");
//    }
//
//    public byte[] downloadErrorReport(Long importId) {
//        // This would generate an Excel file with error details
//        // Implementation would depend on specific requirements
//        throw new UnsupportedOperationException("Error report download not yet implemented");
//    }
//}
