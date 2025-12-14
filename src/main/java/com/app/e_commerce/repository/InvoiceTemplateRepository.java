package com.app.e_commerce.repository;

import com.app.e_commerce.Enum.TemplateStatus;
import com.app.e_commerce.entity.InvoiceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for InvoiceTemplate entity.
 */
@Repository
public interface InvoiceTemplateRepository extends JpaRepository<InvoiceTemplate, Long> {

    /**
     * Find the currently active template
     */
    Optional<InvoiceTemplate> findByStatusAndIsDefaultTrue(TemplateStatus status);

    /**
     * Find all active templates
     */
    List<InvoiceTemplate> findByStatus(TemplateStatus status);

    /**
     * Find templates by name (case-insensitive)
     */
    List<InvoiceTemplate> findByNameContainingIgnoreCase(String name);

    /**
     * Find the default template
     */
    Optional<InvoiceTemplate> findByIsDefaultTrue();

    /**
     * Find templates by status ordered by version descending
     */
    List<InvoiceTemplate> findByStatusOrderByVersionDesc(TemplateStatus status);

    /**
     * Find the latest version of a template by name
     */
    @Query("SELECT t FROM InvoiceTemplate t WHERE t.name = :name ORDER BY t.version DESC LIMIT 1")
    Optional<InvoiceTemplate> findLatestVersionByName(@Param("name") String name);

    /**
     * Count templates by status
     */
    long countByStatus(TemplateStatus status);

    /**
     * Set all templates as non-default
     */
    @Modifying
    @Query("UPDATE InvoiceTemplate t SET t.isDefault = false WHERE t.isDefault = true")
    void clearAllDefaults();

    /**
     * Deactivate all active templates
     */
    @Modifying
    @Query("UPDATE InvoiceTemplate t SET t.status = :newStatus WHERE t.status = :currentStatus")
    void updateStatusForAll(
        @Param("currentStatus") TemplateStatus currentStatus,
        @Param("newStatus") TemplateStatus newStatus
    );

    /**
     * Check if a template name already exists
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find templates created by a specific user
     */
    List<InvoiceTemplate> findByCreatedBy(String createdBy);
}
