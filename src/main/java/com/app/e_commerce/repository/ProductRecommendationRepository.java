package com.app.e_commerce.repository;

import com.app.e_commerce.entity.Product;
import com.app.e_commerce.entity.ProductRecommendation;
import com.app.e_commerce.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ProductRecommendation entity.
 * Provides methods to access and manipulate product recommendations.
 */
@Repository
public interface ProductRecommendationRepository extends JpaRepository<ProductRecommendation, Long> {

    /**
     * Find all recommendations for a specific user, ordered by score (highest first).
     *
     * @param user The user to find recommendations for
     * @return List of product recommendations for the user
     */
    List<ProductRecommendation> findByUserOrderByScoreDesc(User user);

    /**
     * Find top N recommendations for a specific user, ordered by score (highest first).
     *
     * @param user The user to find recommendations for
     * @param pageable Pagination information including page size (limit)
     * @return List of product recommendations for the user
     */
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.user = :user ORDER BY pr.score DESC")
    List<ProductRecommendation> findByUserOrderByScoreDescLimit(@Param("user") User user, Pageable pageable);

    /**
     * Delete all recommendations for a specific user.
     *
     * @param user The user to delete recommendations for
     */
    void deleteByUser(User user);

    /**
     * Check if a recommendation already exists for a user and product.
     *
     * @param user The user
     * @param product The product
     * @return true if a recommendation exists, false otherwise
     */
    boolean existsByUserAndProduct(User user, Product product);
}
