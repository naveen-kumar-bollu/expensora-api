package com.expensora.expensora_api.service;

import com.expensora.expensora_api.entity.Category;
import com.expensora.expensora_api.entity.CategoryType;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    Category createCategory(Category category);
    Category updateCategory(UUID id, Category category);
    void deleteCategory(UUID id);
    List<Category> findByUser(UUID userId);
    List<Category> findByUserAndType(UUID userId, CategoryType type);
    List<Category> findDefaultCategories();
    void initializeDefaultCategories();
}
