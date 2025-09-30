package com.app.e_commerce.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ImportPreviewDTO {
    
    private List<Map<String, Object>> sampleData; // First 10 rows of data
    private List<String> columnHeaders;
    private Map<String, String> suggestedMappings; // Suggested column mappings
    private List<String> validationErrors; // Validation errors found in preview
    private Integer totalRows;
    private boolean hasErrors;
    
    public ImportPreviewDTO() {}
}
