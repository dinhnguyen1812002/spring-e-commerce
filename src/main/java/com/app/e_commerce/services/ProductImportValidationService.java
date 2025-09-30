package com.app.e_commerce.services;

import com.app.e_commerce.DTO.ColumnMappingDTO;
import com.app.e_commerce.entity.Category;
import com.app.e_commerce.entity.ImportError;
import com.app.e_commerce.repository.CategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ProductImportValidationService {
    
    @Autowired
    private CategoryRepo categoryRepo;
    
    private static final Set<String> REQUIRED_FIELDS = Set.of("name", "price");
    private static final Set<String> VALID_PRODUCT_FIELDS = Set.of(
        "name", "price", "description", "category", "stock", "image"
    );
    
    public ColumnMappingDTO getColumnMappingInfo(List<String> availableColumns) {
        ColumnMappingDTO mappingInfo = new ColumnMappingDTO();
        
        mappingInfo.setAvailableColumns(availableColumns);
//        mappingInfo.setProductFields(new ArrayList<>(VALID_PRODUCT_FIELDS));
        mappingInfo.setRequiredFields(new ArrayList<>(REQUIRED_FIELDS));
        mappingInfo.setCurrentMappings(new HashMap<>());
        
        return mappingInfo;
    }
    
    public List<ImportError> validateRowData(Map<String, Object> rowData, int rowNumber, 
                                           Map<String, String> columnMappings) {
        List<ImportError> errors = new ArrayList<>();
        
        // Validate required fields
        for (String requiredField : REQUIRED_FIELDS) {
            if (!columnMappings.containsValue(requiredField)) {
                ImportError error = new ImportError();
                error.setRowNumber(rowNumber);
                error.setColumnName(requiredField);
                error.setErrorMessage("Required field '" + requiredField + "' is not mapped");
                error.setErrorType(ImportError.ErrorType.REQUIRED_FIELD_MISSING);
                errors.add(error);
            }
        }
        
        // Validate each mapped field
        for (Map.Entry<String, String> mapping : columnMappings.entrySet()) {
            String excelColumn = mapping.getKey();
            String productField = mapping.getValue();
            Object value = rowData.get(productField);
            
            List<ImportError> fieldErrors = validateFieldValue(value, productField, excelColumn, rowNumber);
            errors.addAll(fieldErrors);
        }
        
        return errors;
    }
    
    private List<ImportError> validateFieldValue(Object value, String fieldName, String columnName, int rowNumber) {
        List<ImportError> errors = new ArrayList<>();
        
        if (value == null || value.toString().trim().isEmpty()) {
            if (REQUIRED_FIELDS.contains(fieldName)) {
                ImportError error = new ImportError();
                error.setRowNumber(rowNumber);
                error.setColumnName(columnName);
                error.setErrorMessage("Required field '" + fieldName + "' cannot be empty");
                error.setErrorType(ImportError.ErrorType.REQUIRED_FIELD_MISSING);
                errors.add(error);
            }
            return errors;
        }
        
        String stringValue = value.toString().trim();
        
        switch (fieldName) {
            case "name":
                if (stringValue.length() > 255) {
                    errors.add(createError(rowNumber, columnName, "Product name cannot exceed 255 characters", 
                                         stringValue, ImportError.ErrorType.VALIDATION_ERROR));
                }
                break;
                
            case "price":
                try {
                    BigDecimal price = new BigDecimal(stringValue);
                    if (price.compareTo(BigDecimal.ZERO) < 0) {
                        errors.add(createError(rowNumber, columnName, "Price cannot be negative", 
                                             stringValue, ImportError.ErrorType.INVALID_PRICE));
                    }
                } catch (NumberFormatException e) {
                    errors.add(createError(rowNumber, columnName, "Invalid price format", 
                                         stringValue, ImportError.ErrorType.INVALID_PRICE));
                }
                break;
                
            case "description":
                if (stringValue.length() > 2000) {
                    errors.add(createError(rowNumber, columnName, "Description cannot exceed 2000 characters", 
                                         stringValue, ImportError.ErrorType.VALIDATION_ERROR));
                }
                break;
                
            case "category":
                if (!isValidCategory(stringValue)) {
                    errors.add(createError(rowNumber, columnName, "Category '" + stringValue + "' does not exist", 
                                         stringValue, ImportError.ErrorType.INVALID_CATEGORY));
                }
                break;
                
            case "stock":
                try {
                    Integer stock = Integer.parseInt(stringValue);
                    if (stock < 0) {
                        errors.add(createError(rowNumber, columnName, "Stock cannot be negative", 
                                             stringValue, ImportError.ErrorType.INVALID_STOCK));
                    }
                } catch (NumberFormatException e) {
                    errors.add(createError(rowNumber, columnName, "Invalid stock format", 
                                         stringValue, ImportError.ErrorType.INVALID_STOCK));
                }
                break;
                
            case "image":
                if (!isValidImageUrl(stringValue)) {
                    errors.add(createError(rowNumber, columnName, "Invalid image URL format", 
                                         stringValue, ImportError.ErrorType.FORMAT_ERROR));
                }
                break;
        }
        
        return errors;
    }
    
    private boolean isValidCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return false;
        }
        
        Category category = categoryRepo.findByName(categoryName.trim());
        return category != null;
    }
    
    private boolean isValidImageUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return true; // Image is optional
        }
        
        String trimmedUrl = url.trim();
        return trimmedUrl.startsWith("http://") || 
               trimmedUrl.startsWith("https://") || 
               trimmedUrl.startsWith("/") ||
               trimmedUrl.matches("^[a-zA-Z0-9._-]+\\.(jpg|jpeg|png|gif|webp)$");
    }
    
    private ImportError createError(int rowNumber, String columnName, String message, 
                                  String cellValue, ImportError.ErrorType errorType) {
        ImportError error = new ImportError();
        error.setRowNumber(rowNumber);
        error.setColumnName(columnName);
        error.setErrorMessage(message);
        error.setCellValue(cellValue);
        error.setErrorType(errorType);
        return error;
    }
    
    public Map<String, Object> convertRowData(Map<String, Object> rowData, Map<String, String> columnMappings) {
        Map<String, Object> convertedData = new HashMap<>();
        
        for (Map.Entry<String, String> mapping : columnMappings.entrySet()) {
            String productField = mapping.getValue();
            Object value = rowData.get(productField);
            
            if (value != null) {
                Object convertedValue = convertFieldValue(value, productField);
                convertedData.put(productField, convertedValue);
            }
        }
        
        return convertedData;
    }
    
    private Object convertFieldValue(Object value, String fieldName) {
        if (value == null) {
            return null;
        }
        
        String stringValue = value.toString().trim();
        
        switch (fieldName) {
            case "price":
                try {
                    return new BigDecimal(stringValue);
                } catch (NumberFormatException e) {
                    return value; // Return original value if conversion fails
                }
                
            case "stock":
                try {
                    return Integer.parseInt(stringValue);
                } catch (NumberFormatException e) {
                    return value; // Return original value if conversion fails
                }
                
            default:
                return stringValue;
        }
    }
}
