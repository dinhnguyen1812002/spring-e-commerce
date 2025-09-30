package com.app.e_commerce.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_import")
@Getter
@Setter
public class ProductImport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String originalFileName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportStatus status;
    
    @Column(nullable = false)
    private Integer totalRows;
    
    @Column(nullable = false)
    private Integer processedRows;
    
    @Column(nullable = false)
    private Integer successfulImports;
    
    @Column(nullable = false)
    private Integer failedImports;
    
    @Column(length = 2000)
    private String errorMessage;
    
    @Column(nullable = false)
    private LocalDateTime startedAt;
    
    private LocalDateTime completedAt;
    
    @Column(length = 2000)
    private String mappingConfiguration; // JSON string storing column mappings
    
    @OneToMany(mappedBy = "productImport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ImportError> errors = new ArrayList<>();
    
    @PrePersist
    public void prePersist() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ImportStatus.PENDING;
        }
        if (totalRows == null) totalRows = 0;
        if (processedRows == null) processedRows = 0;
        if (successfulImports == null) successfulImports = 0;
        if (failedImports == null) failedImports = 0;
    }
    
    public enum ImportStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}
