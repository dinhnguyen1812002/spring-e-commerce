package com.app.e_commerce.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "import_error")
@Getter
@Setter
public class ImportError {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_import_id", nullable = false)
    private ProductImport productImport;
    
    @Column(nullable = false)
    private Integer rowNumber;
    
    @Column(nullable = false)
    private String columnName;
    
    @Column(length = 1000)
    private String errorMessage;
    
    @Column(length = 2000)
    private String cellValue;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ErrorType errorType;
    
    public enum ErrorType {
        VALIDATION_ERROR,
        DUPLICATE_ERROR,
        FORMAT_ERROR,
        REQUIRED_FIELD_MISSING,
        INVALID_CATEGORY,
        INVALID_PRICE,
        INVALID_STOCK,
        FILE_ERROR
    }
}
