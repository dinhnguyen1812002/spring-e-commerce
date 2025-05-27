package com.app.e_commerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity to store product recommendations for users.
 * This entity is used by the recommendation system to track which products
 * are recommended to which users, along with a score indicating the strength
 * of the recommendation.
 */
@Entity
@Table(name = "product_recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Score representing the strength of the recommendation.
     * Higher values indicate stronger recommendations.
     */
    private Double score;

    /**
     * Timestamp when this recommendation was generated.
     */
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}