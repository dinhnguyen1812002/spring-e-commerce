package com.app.e_commerce.controller;

import com.app.e_commerce.entity.*;
import com.app.e_commerce.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;
    @Autowired
    private OrderService orderService;

    @Autowired
    private TrafficService trafficService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Add dashboard statistics
        List<Order> orders = orderService.getAllOrders();
        long totalProducts = productService.countProducts();
        long totalUsers = userService.countUsers();
        long totalCategories = categoryService.countCategories();
        
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("orders", orders);
        return "admin/admin-dashboard";
    }
//    @GetMapping("/products/edit/{id}")
//    public String editProduct(@PathVariable Long id, Model model) {
//        Product product = productService.findById(id); // Sử dụng findById thay vì getProductById
//        model.addAttribute("product", product);
//        model.addAttribute("categories", categoryService.getAllCategories());
//        return "admin/admin-edit-product";
//    }
    @GetMapping("/products")
    public String listProducts(Model model,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "5") int size) {
        Page<Product> productPage = productService.listProduct(page, size);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("size", size);
        return "admin/admin-products"; // Thymeleaf view name
    }

    @GetMapping("/products/add")
    public String addProduct(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "product/add-product";
    }

    @GetMapping("/products/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Optional<Product> product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "product/edit-product";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "admin/admin-categories";
    }

    @GetMapping("/categories/add")
    public String addCategory(Model model) {
        model.addAttribute("category", new Category());
        return "Category/category-form";
    }

    @GetMapping("/users")
    public String users(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/admin-users";
    }

    @GetMapping("/traffic")
    public String traffic(Model model) {
        List<Traffic> visits = trafficService.getAllVisits();
        model.addAttribute("visits", visits);
        return "admin/admin-traffic";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        // Add order management logic here
        return "admin/admin-orders";
    }
//
//    @GetMapping("/revenue")
//    public String revenue(Model model) {
//        // Add revenue management logic here
//        return "admin/admin-revenue";
//    }
} 