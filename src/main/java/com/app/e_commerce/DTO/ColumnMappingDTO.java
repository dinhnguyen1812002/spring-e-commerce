package com.app.e_commerce.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ColumnMappingDTO {
    
    private List<String> availableColumns; // Available columns from Excel/Sheet
    private Map<String, String> productFields; // Available product fields for mapping
    private Map<String, String> currentMappings; // Current column -> field mappings
    private List<String> requiredFields; // Required product fields
    
    public ColumnMappingDTO() {}
}
