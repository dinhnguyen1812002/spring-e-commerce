package com.app.e_commerce.controller;

import com.app.e_commerce.DTO.invoice.InvoiceTemplateDTO;
import com.app.e_commerce.Enum.TemplateStatus;
import com.app.e_commerce.entity.InvoiceTemplate;
import com.app.e_commerce.entity.User;
import com.app.e_commerce.repository.InvoiceTemplateRepository;
import com.app.e_commerce.services.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/invoice-templates")
@RequiredArgsConstructor
@Slf4j
public class AdminInvoiceTemplateController {

    private final InvoiceTemplateRepository templateRepository;

    @GetMapping
    public String listTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<InvoiceTemplate> templates;

        if (status != null && !status.isEmpty()) {
            templates = templateRepository.findAll(pageable);
        } else {
            templates = templateRepository.findAll(pageable);
        }

        model.addAttribute("templates", templates);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", templates.getTotalPages());
        model.addAttribute("totalItems", templates.getTotalElements());
        model.addAttribute("statuses", TemplateStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("title", "Quản lý Template Hóa đơn");
        model.addAttribute("pageTitle", "Quản lý Template Hóa đơn");
        model.addAttribute("pageDescription", "Tạo và quản lý các mẫu hóa đơn");

        return "admin/invoice-templates";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("template", new InvoiceTemplateDTO());
        model.addAttribute("statuses", TemplateStatus.values());
        model.addAttribute("title", "Tạo Template mới");
        model.addAttribute("pageTitle", "Tạo Template Hóa đơn");
        model.addAttribute("pageDescription", "Tạo mẫu hóa đơn mới");
        model.addAttribute("isEdit", false);

        return "admin/invoice-template-form";
    }

    @PostMapping("/create")
    public String createTemplate(
            @Valid @ModelAttribute("template") InvoiceTemplateDTO templateDTO,
            BindingResult result,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("statuses", TemplateStatus.values());
            model.addAttribute("title", "Tạo Template mới");
            model.addAttribute("pageTitle", "Tạo Template Hóa đơn");
            model.addAttribute("isEdit", false);
            return "admin/invoice-template-form";
        }

        try {
            InvoiceTemplate template = new InvoiceTemplate();
            template.setName(templateDTO.getName());
            template.setDescription(templateDTO.getDescription());
            template.setHtmlContent(templateDTO.getHtmlContent());
            template.setPrimaryColor(templateDTO.getPrimaryColor());
            template.setSecondaryColor(templateDTO.getSecondaryColor());
            template.setFontFamily(templateDTO.getFontFamily());
            template.setCustomCss(templateDTO.getCustomCss());
            template.setStatus(templateDTO.getStatus() != null ? templateDTO.getStatus() : TemplateStatus.INACTIVE);
            template.setIsDefault(templateDTO.getIsDefault() != null && templateDTO.getIsDefault());
            template.setCreatedBy(userDetails.getUsername());
            template.setLastModifiedBy(userDetails.getUsername());

            if (template.getIsDefault()) {
                templateRepository.clearAllDefaults();
            }

            templateRepository.save(template);
            redirectAttributes.addFlashAttribute("success", "Template đã được tạo thành công!");
        } catch (Exception e) {
            log.error("Error creating template", e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi tạo template: " + e.getMessage());
        }

        return "redirect:/admin/invoice-templates";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        InvoiceTemplate template = templateRepository.findById(id).orElse(null);

        if (template == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy template!");
            return "redirect:/admin/invoice-templates";
        }

        InvoiceTemplateDTO dto = mapToDTO(template);
        model.addAttribute("template", dto);
        model.addAttribute("statuses", TemplateStatus.values());
        model.addAttribute("title", "Chỉnh sửa Template");
        model.addAttribute("pageTitle", "Chỉnh sửa Template Hóa đơn");
        model.addAttribute("pageDescription", "Cập nhật mẫu hóa đơn: " + template.getName());
        model.addAttribute("isEdit", true);

        return "admin/invoice-template-form";
    }

    @PostMapping("/edit/{id}")
    public String updateTemplate(
            @PathVariable Long id,
            @Valid @ModelAttribute("template") InvoiceTemplateDTO templateDTO,
            BindingResult result,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("statuses", TemplateStatus.values());
            model.addAttribute("title", "Chỉnh sửa Template");
            model.addAttribute("pageTitle", "Chỉnh sửa Template Hóa đơn");
            model.addAttribute("isEdit", true);
            return "admin/invoice-template-form";
        }

        try {
            InvoiceTemplate template = templateRepository.findById(id).orElse(null);
            if (template == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy template!");
                return "redirect:/admin/invoice-templates";
            }

            template.setName(templateDTO.getName());
            template.setDescription(templateDTO.getDescription());
            template.setHtmlContent(templateDTO.getHtmlContent());
            template.setPrimaryColor(templateDTO.getPrimaryColor());
            template.setSecondaryColor(templateDTO.getSecondaryColor());
            template.setFontFamily(templateDTO.getFontFamily());
            template.setCustomCss(templateDTO.getCustomCss());
            template.setStatus(templateDTO.getStatus());
            template.setLastModifiedBy(userDetails.getUsername());

            if (templateDTO.getIsDefault() != null && templateDTO.getIsDefault() && !template.getIsDefault()) {
                templateRepository.clearAllDefaults();
                template.setIsDefault(true);
            }

            templateRepository.save(template);
            redirectAttributes.addFlashAttribute("success", "Template đã được cập nhật thành công!");
        } catch (Exception e) {
            log.error("Error updating template", e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật template: " + e.getMessage());
        }

        return "redirect:/admin/invoice-templates";
    }

    @PostMapping("/delete/{id}")
    public String deleteTemplate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            InvoiceTemplate template = templateRepository.findById(id).orElse(null);
            if (template == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy template!");
                return "redirect:/admin/invoice-templates";
            }

            templateRepository.delete(template);
            redirectAttributes.addFlashAttribute("success", "Template đã được xóa thành công!");
        } catch (Exception e) {
            log.error("Error deleting template", e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa template: " + e.getMessage());
        }

        return "redirect:/admin/invoice-templates";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            InvoiceTemplate template = templateRepository.findById(id).orElse(null);
            if (template == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy template!");
                return "redirect:/admin/invoice-templates";
            }

            if (template.getStatus() == TemplateStatus.ACTIVE) {
                template.setStatus(TemplateStatus.INACTIVE);
            } else {
                template.setStatus(TemplateStatus.ACTIVE);
            }

            templateRepository.save(template);
            redirectAttributes.addFlashAttribute("success", "Trạng thái template đã được cập nhật!");
        } catch (Exception e) {
            log.error("Error toggling template status", e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/invoice-templates";
    }

    @PostMapping("/set-default/{id}")
    public String setDefault(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            InvoiceTemplate template = templateRepository.findById(id).orElse(null);
            if (template == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy template!");
                return "redirect:/admin/invoice-templates";
            }

            templateRepository.clearAllDefaults();
            template.setIsDefault(true);
            templateRepository.save(template);
            redirectAttributes.addFlashAttribute("success", "Đã đặt template làm mặc định!");
        } catch (Exception e) {
            log.error("Error setting default template", e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/invoice-templates";
    }

    private InvoiceTemplateDTO mapToDTO(InvoiceTemplate template) {
        InvoiceTemplateDTO dto = new InvoiceTemplateDTO();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setDescription(template.getDescription());
        dto.setHtmlContent(template.getHtmlContent());
        dto.setPrimaryColor(template.getPrimaryColor());
        dto.setSecondaryColor(template.getSecondaryColor());
        dto.setFontFamily(template.getFontFamily());
        dto.setCustomCss(template.getCustomCss());
        dto.setStatus(template.getStatus());
        dto.setVersion(template.getVersion());
        dto.setIsDefault(template.getIsDefault());
        dto.setCreatedAt(template.getCreatedAt());
        dto.setUpdatedAt(template.getUpdatedAt());
        dto.setCreatedBy(template.getCreatedBy());
        dto.setLastModifiedBy(template.getLastModifiedBy());
        dto.setLogoFileName(template.getLogoFileName());
        dto.setHasLogo(template.getLogoImage() != null);
        return dto;
    }
}
