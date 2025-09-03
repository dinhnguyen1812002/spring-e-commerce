package com.app.e_commerce.controller;

import com.app.e_commerce.entity.Category;
import com.app.e_commerce.entity.Product;
import com.app.e_commerce.entity.Rate;
import com.app.e_commerce.exception.ProductNotFoundException;
import com.app.e_commerce.services.CategoryService;
import com.app.e_commerce.services.ProductService;
import com.app.e_commerce.services.RateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;

import java.util.*;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RateService rateService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping("/new")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "product/add-product";
    }

    @PostMapping("/create")
    public String addProduct(@ModelAttribute Product product,
                             @RequestParam("file") MultipartFile file
    ) throws IOException {

        productService.saveProduct(product, file);
        return "redirect:/admin/products";
    }

    private Set<Category> processCategories(String categoriesString) {
        Set<Category> categories = new HashSet<>();
        String[] categoryNames = categoriesString.split(",");
        for (String categoryName : categoryNames) {
            Category category = categoryService.findOrCreateCategoryByName(categoryName.trim());
            if (category != null) {
                categories.add(category);
            } else {
                throw new RuntimeException("Not found category: " + categoryName);
            }
        }
        return categories;
    }

    @GetMapping
    public String listProducts(Model model,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "5") int size,
                               @RequestParam(value = "category", required = false) String categorySlug) {

        Page<Product> productPage;

        if (categorySlug != null && !categorySlug.isEmpty()) {
            // Lọc theo category (slug)
            productPage = productService.listProductsByCategorySlug(categorySlug, page, size);
            model.addAttribute("selectedCategory", categorySlug);
        } else {
            // Hiển thị tất cả
            productPage = productService.listProduct(page, size);
            model.addAttribute("selectedCategory", "");
        }

        List<Category> categories = categoryService.getAllCategories();

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("size", size);

        return "product/products"; // Thymeleaf view name
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam("keyword") String keyword, Model model) {
        List<Product> searchResults = productService.searchProducts(keyword.trim().toLowerCase());
        List<Category> categories  = categoryService.getAllCategories();
        model.addAttribute("products", searchResults);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 1);
        model.addAttribute("totalItems", searchResults.size());
        model.addAttribute("size", searchResults.size());

        return "product/products";
    }

    @GetMapping("/edit/{id}")
    public String showUpdateProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        return "product/edit-product";
    }


    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute("product") Product product,
                                @RequestParam("file") MultipartFile file,
                                RedirectAttributes redirectAttributes) {
        try {
            product.setId(id);
            productService.saveProduct(product, file);
            redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update product!");
        }
        return "redirect:/products";
    }

    // New method for deleting a product
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, Model model) {
        try {
            productService.deleteProduct(id);
            return "redirect:/products";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi xóa sản phẩm: " + e.getMessage());
            return "redirect:/products";
        }
    }
    @GetMapping("/{id}")
    public String getProductById(@PathVariable Long id, Model model) {
        Optional<Product> productOptional = productService.getProductById(id);
        List<Rate> rates = rateService.getRatesByProduct(id);
        
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            
            // Tính rating trung bình
            double averageRating = rateService.getAverageRating(id);
            long ratingCount = rateService.getRatingCount(id);
            int[] ratingDistribution = rateService.getRatingDistribution(id);
            
            model.addAttribute("product", product);
            model.addAttribute("rates", rates);
            model.addAttribute("averageRating", averageRating);
            model.addAttribute("ratingCount", ratingCount);
            model.addAttribute("ratingDistribution", ratingDistribution);
            
            return "product/product-details";
        } else {
            model.addAttribute("errorMessage", "Product not found");
            return "product/product-not-found";
        }
    }

//    @GetMapping("/find")
//    @ResponseBody
//    public List<Product> searchProducts(@RequestParam("query") String query) {
//        return productService.searchProducts(query);
//    }

}
