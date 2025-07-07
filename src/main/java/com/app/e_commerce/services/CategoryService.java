package com.app.e_commerce.services;

import com.app.e_commerce.entity.Category;
import com.app.e_commerce.repository.CategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepo categoryRepository;

    public void saveCategory(Category category) {
        categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
    public Category findByName(String name) {
        return categoryRepository.findByName(name);
    }

        public Category findOrCreateCategoryByName(String name) {
        Category category = findByName(name);
        if (category == null) {
            category = new Category();
            category.setName(name);
            category = categoryRepository.save(category);
        }
        return category;
    }

    public long countCategories() {
        return categoryRepository.count();
    }

}
