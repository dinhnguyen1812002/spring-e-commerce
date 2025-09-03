package com.app.e_commerce.controller;
import com.app.e_commerce.entity.Category;
import com.app.e_commerce.entity.Product;
import com.app.e_commerce.entity.User;
import com.app.e_commerce.repository.UserRepo;
import com.app.e_commerce.services.CategoryService;
import com.app.e_commerce.services.ProductService;
import com.app.e_commerce.services.RecommendationService;
import com.app.e_commerce.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping
public class HomeController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RecommendationService recommendationService;

    @GetMapping
    public String home(HttpServletRequest request, Model model)
    {
        request.getSession(); // ép tạo session ngay từ đầu
        List<Product> listProduct = productService.getAllProducts();
        List<Category> categories = categoryService.getAllCategories();

        // Get recommended products (limit to 4)
        List<Product> recommendedProducts = recommendationService.getRecommendedProducts(4, null);

        // Get popular products (limit to 4)
        List<Product> popularProducts = recommendationService.getPopularProducts(4, null);

        // Get latest products
        List<Product> newProducts = productService.getLatestProducts();

        model.addAttribute("products", listProduct);
        model.addAttribute("categories", categories);
        model.addAttribute("recommendedProducts", recommendedProducts);
        model.addAttribute("popularProducts", popularProducts);
        model.addAttribute("newProducts", newProducts);

        return "home";
    }
    @GetMapping("/user-list")
    public String getAllUser(Model model){
        List<User> listUser = userService.getAllUser();
        model.addAttribute("users", listUser);
        return "users/list-user";
    }
    @GetMapping("/search")
    public String searchProducts(@RequestParam("keyword") String keyword, Model model) {
        model.addAttribute("products", productService.searchProducts(keyword));
        return "fragments/header";
    }

    @GetMapping("/hero")
    public String hero(){
        return "fragments/hero";
    }

}
