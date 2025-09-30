package com.app.e_commerce.DTO;

import com.app.e_commerce.entity.ProductImport;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ProductImportResponseDTO {
    
    private Long id;
    private String fileName;
    private String originalFileName;
    private ProductImport.ImportStatus status;
    private Integer totalRows;
    private Integer processedRows;
    private Integer successfulImports;
    private Integer failedImports;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Double progressPercentage;
    private List<ImportErrorDTO> errors;
    
    public ProductImportResponseDTO() {}
    
    public ProductImportResponseDTO(ProductImport productImport) {
        this.id = productImport.getId();
        this.fileName = productImport.getFileName();
        this.originalFileName = productImport.getOriginalFileName();
        this.status = productImport.getStatus();
        this.totalRows = productImport.getTotalRows();
        this.processedRows = productImport.getProcessedRows();
        this.successfulImports = productImport.getSuccessfulImports();
        this.failedImports = productImport.getFailedImports();
        this.errorMessage = productImport.getErrorMessage();
        this.startedAt = productImport.getStartedAt();
        this.completedAt = productImport.getCompletedAt();
        
        if (totalRows != null && totalRows > 0) {
            this.progressPercentage = (double) processedRows / totalRows * 100;
        } else {
            this.progressPercentage = 0.0;
        }
    }
}
