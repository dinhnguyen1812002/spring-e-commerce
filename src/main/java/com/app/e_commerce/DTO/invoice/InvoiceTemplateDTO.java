package com.app.e_commerce.DTO.invoice;

import com.app.e_commerce.Enum.TemplateStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * DTO for invoice template management
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceTemplateDTO {

    private Long id;

    @NotBlank(message = "Template name is required")
    @Size(max = 255, message = "Template name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "HTML content is required")
    private String htmlContent;

    // Logo upload (for create/update operations)
    private MultipartFile logoFile;

    // Logo information (for responses)
    private String logoFileName;
    private String logoContentType;
    private Boolean hasLogo;

    // Customization fields
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Primary color must be a valid hex color (e.g., #3B82F6)")
    private String primaryColor = "#3B82F6";

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Secondary color must be a valid hex color (e.g., #1E40AF)")
    private String secondaryColor = "#1E40AF";

    @Size(max = 100, message = "Font family must not exceed 100 characters")
    private String fontFamily = "Arial, sans-serif";

    @Size(max = 5000, message = "Custom CSS must not exceed 5000 characters")
    private String customCss;

    private TemplateStatus status;
    private Integer version;
    private Boolean isDefault;

    // Audit information
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String lastModifiedBy;
}
