package com.app.e_commerce.controller;

import com.app.e_commerce.entity.Category;
import com.app.e_commerce.entity.Product;
import com.app.e_commerce.services.CategoryService;
import com.app.e_commerce.services.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Controller for handling product recommendations.
 */
@Controller
@RequestMapping("/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private CategoryService categoryService;
    /**
     * Get recommended products for the current user.
     * 
     * @param model The model to add attributes to
     * @param limit Maximum number of recommendations to return (default: 4)
     * @return The recommendations view
     */
    @GetMapping
    public String getRecommendations(Model model,
                                     @RequestParam(defaultValue = "4") int limit,
                                     @RequestParam(required = false) String category
                                     ) {
        List<Product> recommendedProducts = recommendationService.getRecommendedProducts(limit, category);
        List<Category > categories =  categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("products", recommendedProducts);
        model.addAttribute("selectedCategory", category); // để giữ trạng thái filter
        return "product/products"; // Thymeleaf view name
    }

    /**
     * Get recommended products as JSON for AJAX requests.
     * 
     * @param limit Maximum number of recommendations to return (default: 4)
     * @return List of recommended products
     */
    @GetMapping("/api")
    @ResponseBody
    public List<Product> getRecommendationsApi(@RequestParam(defaultValue = "4") int limit,
                                               @RequestParam(required = false) String category

                                               ) {
        return recommendationService.getRecommendedProducts(limit, category);
    }

    /**
     * Get popular products.
     * 
     * @param model The model to add attributes to
     * @param limit Maximum number of products to return (default: 4)
     * @return The popular products view
     */
    @GetMapping("/popular")
    public String getPopularProducts(Model model,
                                     @RequestParam(defaultValue = "4") int limit,
                                     @RequestParam(required = false) String category) {
        List<Product> popularProducts = recommendationService.getPopularProducts(limit, category);
        List<Category> categories = categoryService.getAllCategories();

        model.addAttribute("categories", categories);
        model.addAttribute("products", popularProducts);
        model.addAttribute("selectedCategory", category);
        return "product/products";
    }
}