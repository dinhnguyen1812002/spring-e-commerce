package com.app.e_commerce.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.e_commerce.entity.Category;
import com.app.e_commerce.entity.Order;
import com.app.e_commerce.entity.Product;
import com.app.e_commerce.entity.Traffic;
import com.app.e_commerce.entity.User;
import com.app.e_commerce.service.impl.DashboardServiceImpl;
import com.app.e_commerce.services.CategoryService;
import com.app.e_commerce.services.OrderService;
import com.app.e_commerce.services.ProductService;
import com.app.e_commerce.services.TrafficService;
import com.app.e_commerce.services.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

//    @Autowired
//    private ProductService productService;

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
@Autowired
private DashboardServiceImpl dashboardService;
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Add dashboard statistics
        List<Order> recentOrders = orderService.getRecentOrders(5); // Get 5 most recent orders
        long totalProducts = productService.countProducts();
        long totalUsers = userService.countUsers();
        long totalCategories = categoryService.countCategories();

        // Get current month revenue
        LocalDate now = LocalDate.now();
        BigDecimal currentMonthRevenue = dashboardService.getMonthlyRevenue(now.getYear(), now.getMonthValue());
        if (currentMonthRevenue == null) {
            currentMonthRevenue = BigDecimal.ZERO;
        }

        // Calculate growth from last month
        LocalDate lastMonth = now.minusMonths(1);
        BigDecimal lastMonthRevenue = dashboardService.getMonthlyRevenue(lastMonth.getYear(), lastMonth.getMonthValue());
        if (lastMonthRevenue == null) {
            lastMonthRevenue = BigDecimal.ZERO;
        }

        double growthPercentage = 0.0;
        if (lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthPercentage = currentMonthRevenue.subtract(lastMonthRevenue)
                    .divide(lastMonthRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        } else if (currentMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthPercentage = 100.0;
        }

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("totalRevenue", dashboardService.getTotalRevenue() != null ? dashboardService.getTotalRevenue() : BigDecimal.ZERO);
        model.addAttribute("currentMonthRevenue", currentMonthRevenue);
        model.addAttribute("revenueGrowth", growthPercentage);
        model.addAttribute("orders", recentOrders);

        return "admin/dashboard";
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
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        model.addAttribute("product", product);  // ✅ Truyền Product, không phải Optional
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
        // Get traffic data for dashboard
        Map<String, Object> trafficData = trafficService.getTrafficDataForDashboard();

        // Add all traffic data to model
        for (Map.Entry<String, Object> entry : trafficData.entrySet()) {
            model.addAttribute(entry.getKey(), entry.getValue());
        }

        // Add raw visit data for the table
        List<Traffic> visits = trafficService.getAllTraffic();
        model.addAttribute("visits", visits);

        return "admin/admin-traffic";
    }

    @GetMapping("/orders")
    public String orders(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<Order> orderPage = orderService.getAllOrders(PageRequest.of(page, size));
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        return "admin/admin-orders";
    }

    @GetMapping("/product-import")
    public String productImport() {
        return "admin/product-import";
    }

    @GetMapping("/import-history")
    public String importHistory() {
        return "admin/import-history";
    }
//
//    @GetMapping("/revenue")
//    public String revenue(Model model) {
//        // Add revenue management logic here
//        return "admin/admin-revenue";
//    }
} 
