package com.app.e_commerce.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ProductImportRequestDTO {
    
    private String fileName;
    private String originalFileName;
    private String fileType; // EXCEL, GOOGLE_SHEETS
    private String googleSheetsUrl;
    private String googleSheetsId;
    private String sheetName;
    private Map<String, String> columnMappings; // Excel column -> Product field mapping
    private boolean updateExisting; // Whether to update existing products
    private boolean skipDuplicates; // Whether to skip duplicate products
    private Integer batchSize; // Number of rows to process in each batch
    
    public ProductImportRequestDTO() {
        this.batchSize = 100; // Default batch size
        this.updateExisting = false;
        this.skipDuplicates = true;
    }
}
