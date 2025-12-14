package com.app.e_commerce.entity;

import com.app.e_commerce.Enum.TemplateStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing an invoice template for customizing invoice appearance.
 * Supports versioning, custom branding, and HTML/CSS customization.
 */
@Entity
@Table(name = "invoice_templates", indexes = {
    @Index(name = "idx_template_status", columnList = "status"),
    @Index(name = "idx_template_default", columnList = "isDefault")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String htmlContent; // Thymeleaf template content

    @Lob
    @Column(columnDefinition = "BYTEA")
    private byte[] logoImage;

    @Column(length = 100)
    private String logoFileName;

    @Column(length = 50)
    private String logoContentType;

    // Customization fields
    @Column(length = 7)
    private String primaryColor = "#3B82F6"; // Default blue

    @Column(length = 7)
    private String secondaryColor = "#1E40AF"; // Default dark blue

    @Column(length = 100)
    private String fontFamily = "Arial, sans-serif";

    @Column(columnDefinition = "TEXT")
    private String customCss;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TemplateStatus status = TemplateStatus.INACTIVE;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(nullable = false)
    private Boolean isDefault = false;

    // Audit fields
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false, updatable = false, length = 255)
    private String createdBy;

    @Column(nullable = false, length = 255)
    private String lastModifiedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if template is active
     */
    public boolean isActive() {
        return status == TemplateStatus.ACTIVE;
    }

    /**
     * Activate this template
     */
    public void activate() {
        this.status = TemplateStatus.ACTIVE;
    }

    /**
     * Deactivate this template
     */
    public void deactivate() {
        this.status = TemplateStatus.INACTIVE;
    }

    /**
     * Archive this template
     */
    public void archive() {
        this.status = TemplateStatus.ARCHIVED;
    }
}
