package com.app.e_commerce.repository;

import com.app.e_commerce.entity.ProductImport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductImportRepository extends JpaRepository<ProductImport, Long> {
    
    List<ProductImport> findByStatusOrderByStartedAtDesc(ProductImport.ImportStatus status);
    
    Page<ProductImport> findAllByOrderByStartedAtDesc(Pageable pageable);
    
    @Query("SELECT pi FROM ProductImport pi WHERE pi.startedAt >= :startDate AND pi.startedAt <= :endDate ORDER BY pi.startedAt DESC")
    List<ProductImport> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(pi) FROM ProductImport pi WHERE pi.status = :status")
    Long countByStatus(ProductImport.ImportStatus status);
    
    @Query("SELECT pi FROM ProductImport pi WHERE pi.fileName = :fileName ORDER BY pi.startedAt DESC")
    List<ProductImport> findByFileName(String fileName);
}
