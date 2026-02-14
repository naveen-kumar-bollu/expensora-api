package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.entity.Category;
import com.expensora.expensora_api.entity.CategoryType;
import com.expensora.expensora_api.repository.CategoryRepository;
import com.expensora.expensora_api.service.CategoryService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(UUID id, Category category) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        existing.setName(category.getName());
        existing.setType(category.getType());
        existing.setColor(category.getColor());
        existing.setIcon(category.getIcon());
        return categoryRepository.save(existing);
    }

    @Override
    public void deleteCategory(UUID id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public List<Category> findByUser(UUID userId) {
        return categoryRepository.findAll().stream()
                .filter(c -> (c.getUser() != null && c.getUser().getId().equals(userId)) || c.getIsDefault())
                .toList();
    }

    @Override
    public List<Category> findByUserAndType(UUID userId, CategoryType type) {
        return findByUser(userId).stream()
                .filter(c -> c.getType() == type)
                .toList();
    }

    @Override
    public List<Category> findDefaultCategories() {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getIsDefault() != null && c.getIsDefault())
                .toList();
    }

    @Override
    @PostConstruct
    public void initializeDefaultCategories() {
        if (categoryRepository.count() == 0) {
            // Default expense categories
            Arrays.asList(
                    createDefaultCategory("Food & Dining", CategoryType.EXPENSE, "#FF6B6B", "🍔"),
                    createDefaultCategory("Transportation", CategoryType.EXPENSE, "#4ECDC4", "🚗"),
                    createDefaultCategory("Shopping", CategoryType.EXPENSE, "#45B7D1", "🛍️"),
                    createDefaultCategory("Entertainment", CategoryType.EXPENSE, "#FFA07A", "🎬"),
                    createDefaultCategory("Bills & Utilities", CategoryType.EXPENSE, "#98D8C8", "💡"),
                    createDefaultCategory("Healthcare", CategoryType.EXPENSE, "#FFB6C1", "🏥"),
                    createDefaultCategory("Education", CategoryType.EXPENSE, "#DDA0DD", "📚"),
                    createDefaultCategory("Others", CategoryType.EXPENSE, "#D3D3D3", "📦"),
                    
                    // Default income categories
                    createDefaultCategory("Salary", CategoryType.INCOME, "#90EE90", "💰"),
                    createDefaultCategory("Business", CategoryType.INCOME, "#87CEEB", "💼"),
                    createDefaultCategory("Investment", CategoryType.INCOME, "#FFD700", "📈"),
                    createDefaultCategory("Others", CategoryType.INCOME, "#D3D3D3", "💵")
            ).forEach(categoryRepository::save);
        }
    }

    private Category createDefaultCategory(String name, CategoryType type, String color, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setType(type);
        category.setColor(color);
        category.setIcon(icon);
        category.setIsDefault(true);
        return category;
    }
}
