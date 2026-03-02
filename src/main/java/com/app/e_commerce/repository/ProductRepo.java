package com.app.e_commerce.repository;

import com.app.e_commerce.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.categories")
    List<Product> findAllWithCategories();

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.categories c WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchProductsByKeyword(@Param("keyword") String keyword);

    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findOrderByCreatedAtDesc();// top 10 newest product

    Page<Product> findByCategories_Slug(String slug, Pageable pageable);

}
