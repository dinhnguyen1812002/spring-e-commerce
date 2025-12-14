package com.app.e_commerce.controller;

import com.app.e_commerce.DTO.invoice.InvoiceFilterDTO;
import com.app.e_commerce.DTO.invoice.InvoiceResponseDTO;
import com.app.e_commerce.entity.User;
import com.app.e_commerce.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/invoices")
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final com.app.e_commerce.repository.UserRepository userRepository;

    /**
     * List invoices page
     */
    @GetMapping
    public String listInvoices(
            @ModelAttribute InvoiceFilterDTO filterDTO,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        User currentUser = getUserFromDetails(userDetails);
        
        // Set default filter values if needed
        if (filterDTO.getSize() == 0) filterDTO.setSize(10);
        if (filterDTO.getSortBy() == null) filterDTO.setSortBy("createdAt");
        if (filterDTO.getSortDirection() == null) filterDTO.setSortDirection("DESC");

        Page<InvoiceResponseDTO> invoices = invoiceService.listInvoices(filterDTO, currentUser);
        
        model.addAttribute("invoices", invoices);
        model.addAttribute("filter", filterDTO);
        
        return "invoices/list";
    }

    /**
     * View invoice detail page
     */
    @GetMapping("/{id}")
    public String viewInvoice(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        User currentUser = getUserFromDetails(userDetails);
        InvoiceResponseDTO invoice = invoiceService.getInvoiceById(id, currentUser);
        
        model.addAttribute("invoice", invoice);
        
        return "invoices/view";
    }

    /**
     * Helper to get current user
     */
    private User getUserFromDetails(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));
    }
}
