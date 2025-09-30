package com.app.e_commerce.repository;

import com.app.e_commerce.entity.ImportError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportErrorRepository extends JpaRepository<ImportError, Long> {
    
    List<ImportError> findByProductImportIdOrderByRowNumber(Long productImportId);
    
    @Query("SELECT ie FROM ImportError ie WHERE ie.productImport.id = :importId AND ie.errorType = :errorType ORDER BY ie.rowNumber")
    List<ImportError> findByProductImportIdAndErrorType(Long importId, ImportError.ErrorType errorType);
    
    @Query("SELECT COUNT(ie) FROM ImportError ie WHERE ie.productImport.id = :importId")
    Long countByProductImportId(Long importId);
    
    @Query("SELECT ie FROM ImportError ie WHERE ie.productImport.id = :importId AND ie.rowNumber = :rowNumber")
    List<ImportError> findByProductImportIdAndRowNumber(Long importId, Integer rowNumber);
}
