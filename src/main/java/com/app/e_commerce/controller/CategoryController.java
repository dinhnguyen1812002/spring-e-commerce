package com.app.e_commerce.controller;

import com.app.e_commerce.entity.Category;
import com.app.e_commerce.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @GetMapping("/new-category")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        return "Category/category-form";

    }
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category") @Valid Category category,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "Category/category-form";
        }

        categoryService.saveCategory(category);

        return "redirect:/admin/categories";
    }
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.getCategoryById(id);
        if (category != null) {
            model.addAttribute("category", category);
            return "Category/category-form";
        } else {
            return "redirect:/categories";
        }
    }
    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable("id") Long id,
                                 @ModelAttribute("category") @Valid Category category,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "Category/category-form";
        }
        // Đảm bảo id được set đúng
        category.setId(id);
        categoryService.saveCategory(category); // service save có thể handle cả create/update
        return "redirect:/admin/categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/admin/categories";
    }
    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "Category/categories";
    }
}

