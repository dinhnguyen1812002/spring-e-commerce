package com.app.e_commerce.services;



import com.app.e_commerce.entity.Category;
import com.app.e_commerce.entity.Product;
import com.app.e_commerce.repository.CategoryRepo;
//
//import com.app.e_commerce.repository.Elasticsearch.ProductELS;
import com.app.e_commerce.repository.ProductRepo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ProductService {

    @Autowired
    private ProductRepo productRepository;
    @Autowired
    private CategoryRepo categoryRepo;
//    @Autowired private ProductELS productELS;

    @Value("${file.upload-dir}")
    private String uploadDir;



    public List<Product> getAllProducts() {
        return (List<Product>) productRepository.findAll(Sort.by("id").descending());
    }
    public Page<Product>listProduct(int page, int size){
        Pageable pageable= PageRequest.of(page, size, Sort.by("id").descending());
        return productRepository.findAll(pageable);
    }
//    public List<Product> getAllProductsWithCategories() {
//        return productRepository.findAllWithCategories();
//    }

//    public Product saveProduct(Product product) {
//        return productRepository.save(product);
//    }

    @Transactional
    public void deleteProduct(Long id) {
        Optional<Product> productOptional = getProductById(id);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();

            // Get the filename of the image
            String imageFileName = product.getImage();

            // Construct the path to the image file
            Path imagePath = Paths.get( uploadDir, imageFileName);

            try {
                // Delete the image file from the server
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete image file: " + imageFileName, e);
            }

            // Now delete the product from the database
            productRepository.deleteById(id);
        } else {
            throw new RuntimeException("Product not found with id: " + id);
        }
    }

    public Product saveProduct(Product product, MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            product.setImage("uploads/" +fileName);
        }

        Set<Category> categories = product.getCategories();
        product.setCategories(new HashSet<>());
        Product savedProduct = productRepository.save(product);

        for (Category category : categories) {
            Category managedCategory = categoryRepo.findById(category.getId()).orElseThrow(() -> new RuntimeException("Category not found"));
            managedCategory.getProducts().add(savedProduct);
            savedProduct.getCategories().add(managedCategory);
        }

        return productRepository.save(savedProduct);
    }
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return (List<Product>) productRepository.findAll();
        }
        return productRepository.searchProductsByKeyword(keyword);
    }

    /**
     * Find product by ID.
     *
     * @param id the product ID
     * @return an Optional containing the product if found, or an empty Optional if not found
     */
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product findById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }
    public List<Product> getLatestProducts() {
        return productRepository.findOrderByCreatedAtDesc();
    }

    public long countProducts() {
        return productRepository.count();
    }

}
