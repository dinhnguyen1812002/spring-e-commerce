package com.app.e_commerce.services;

import com.app.e_commerce.entity.*;
import com.app.e_commerce.repository.OrderRepository;
import com.app.e_commerce.repository.ProductRecommendationRepository;
import com.app.e_commerce.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating and retrieving product recommendations.
 * Uses collaborative filtering based on user purchase history and product categories.
 */
@Service
public class RecommendationService {

    @Autowired
    private ProductRecommendationRepository recommendationRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepo productRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationContext context;

    /**
     * Get recommended products for the current user, with optional category filter.
     *
     * @param limit Maximum number of recommendations to return
     * @param categorySlug Optional category slug to filter by (nullable)
     * @return List of recommended products
     */
    public List<Product> getRecommendedProducts(int limit, String categorySlug) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            // For anonymous users, return popular products
            return getPopularProducts(limit, categorySlug);
        }

        // Get recommendations for the current user
        List<ProductRecommendation> recommendations =
                recommendationRepository.findByUserOrderByScoreDescLimit(
                        currentUser, PageRequest.of(0, limit * 3) // lấy nhiều hơn để lọc
                );

        // If no recommendations exist, generate them
        if (recommendations.isEmpty()) {
            context.getBean(RecommendationService.class).generateRecommendations(currentUser);
            recommendations = recommendationRepository.findByUserOrderByScoreDescLimit(
                    currentUser, PageRequest.of(0, limit * 3)
            );
        }

        // If still no recommendations, return popular products
        if (recommendations.isEmpty()) {
            return getPopularProducts(limit, categorySlug);
        }

        // Extract products and filter by category
        List<Product> products = recommendations.stream()
                .map(ProductRecommendation::getProduct)
                .filter(product -> categorySlug == null ||
                        product.getCategories().stream()
                                .anyMatch(cat -> categorySlug.equals(cat.getSlug())))
                .limit(limit)
                .collect(Collectors.toList());

        // Nếu sau khi lọc rỗng -> fallback sang popular
        if (products.isEmpty()) {
            return getPopularProducts(limit, categorySlug);
        }

        return products;
    }


    /**
     * Get the currently authenticated user.
     * 
     * @return The current user, or null if not authenticated
     */
    private User getCurrentUser() {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return null;
            }

            String username = authentication.getName();
            return userService.findByUsername(username);
        } catch (Exception e) {
            // If there's any error getting the current user, return null
            return null;
        }
    }

    public List<Product> getPopularProducts(int limit, String categorySlug) {
        List<OrderItem> allOrderItems = orderRepository.findAll().stream()
                .flatMap(order -> order.getOrderItems().stream())
                .toList();

        Map<Product, Integer> productCounts = new HashMap<>();
        for (OrderItem item : allOrderItems) {
            productCounts.put(item.getProduct(),
                    productCounts.getOrDefault(item.getProduct(), 0) + item.getQuantity());
        }

        return productCounts.entrySet().stream()
                .map(Map.Entry::getKey)
                .filter(product -> categorySlug == null ||
                        product.getCategories().stream()
                                .anyMatch(cat -> categorySlug.equals(cat.getSlug())))
                .sorted(Comparator.comparingInt(p -> -productCounts.get(p)))
                .limit(limit)
                .collect(Collectors.toList());
    }


    /**
     * Generate recommendations for a specific user.
     * Uses collaborative filtering based on purchase history and product categories.
     *
     * @param user The user to generate recommendations for
     */
    @Transactional
    public void generateRecommendations(User user) {
        // Delete existing recommendations for this user
        recommendationRepository.deleteByUser(user);

        // Get user's purchase history
        List<Order> userOrders = orderRepository.findByUser(user);
        if (userOrders.isEmpty()) {
            // If user has no orders, recommend popular products
            List<Product> popularProducts = getPopularProducts(10, null);
            saveRecommendations(user, popularProducts, 1.0);
            return;
        }

        // Get products the user has purchased
        Set<Product> purchasedProducts = userOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(OrderItem::getProduct)
                .collect(Collectors.toSet());

        // Get categories of purchased products
        Set<Category> purchasedCategories = purchasedProducts.stream()
                .flatMap(product -> product.getCategories().stream())
                .collect(Collectors.toSet());

        // Find products in the same categories that the user hasn't purchased
        Set<Product> recommendedProducts = new HashSet<>();
        for (Category category : purchasedCategories) {
            for (Product product : category.getProducts()) {
                if (!purchasedProducts.contains(product)) {
                    recommendedProducts.add(product);
                }
            }
        }

        // Calculate scores and save recommendations
        for (Product product : recommendedProducts) {
            double score = calculateRecommendationScore(product, purchasedProducts, purchasedCategories);
            saveRecommendation(user, product, score);
        }
    }

    /**
     * Calculate recommendation score for a product.
     * Higher scores indicate stronger recommendations.
     *
     * @param product The product to calculate score for
     * @param purchasedProducts Products the user has purchased
     * @param purchasedCategories Categories of products the user has purchased
     * @return Recommendation score
     */
    private double calculateRecommendationScore(Product product, Set<Product> purchasedProducts, 
                                               Set<Category> purchasedCategories) {
        // Base score
        double score = 0.5;

        // Increase score based on category overlap
        Set<Category> productCategories = product.getCategories();
        long categoryOverlap = productCategories.stream()
                .filter(purchasedCategories::contains)
                .count();

        score += 0.1 * categoryOverlap;

        // Adjust score based on price similarity with purchased products
        double avgPurchasedPrice = purchasedProducts.stream()
                .mapToDouble(p -> p.getPrice().doubleValue())
                .average()
                .orElse(0.0);

        double productPrice = product.getPrice().doubleValue();
        double priceDifference = Math.abs(productPrice - avgPurchasedPrice) / avgPurchasedPrice;

        // Reduce score if price is very different
        if (priceDifference > 0.5) {
            score -= 0.1;
        }

        return Math.min(1.0, Math.max(0.1, score));
    }

    /**
     * Save a recommendation for a user and product.
     *
     * @param user The user
     * @param product The product
     * @param score The recommendation score
     */
    private void saveRecommendation(User user, Product product, double score) {
        ProductRecommendation recommendation = new ProductRecommendation();
        recommendation.setUser(user);
        recommendation.setProduct(product);
        recommendation.setScore(score);
        recommendationRepository.save(recommendation);
    }

    /**
     * Save recommendations for a user and multiple products with the same score.
     *
     * @param user The user
     * @param products The products
     * @param score The recommendation score
     */
    private void saveRecommendations(User user, List<Product> products, double score) {
        for (Product product : products) {
            saveRecommendation(user, product, score);
        }
    }

    /**
     * Regenerate recommendations for all users.
     * Scheduled to run daily at midnight.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void regenerateAllRecommendations() {
        List<User> allUsers = userService.getAllUser();
        for (User user : allUsers) {
            generateRecommendations(user);
        }
    }
}
