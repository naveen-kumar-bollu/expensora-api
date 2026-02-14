package com.expensora.expensora_api.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.expensora.expensora_api.dto.CategoryCreateRequestDto;
import com.expensora.expensora_api.dto.CategoryDto;
import com.expensora.expensora_api.entity.Category;
import com.expensora.expensora_api.entity.User;
import com.expensora.expensora_api.mapper.CategoryMapper;
import com.expensora.expensora_api.repository.UserRepository;
import com.expensora.expensora_api.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/categories")
@Tag(name = "Categories", description = "Category management APIs for income and expense categorization")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a new category", description = "Create a custom category for the authenticated user")
    public ResponseEntity<CategoryDto> create(@RequestBody CategoryCreateRequestDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Category category = new Category();
        category.setName(dto.getName());
        category.setType(dto.getType());
        category.setColor(dto.getColor());
        category.setIcon(dto.getIcon());
        category.setIsDefault(false);
        category.setUser(user);

        Category saved = categoryService.createCategory(category);
        return ResponseEntity.ok(categoryMapper.toDto(saved));
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Get all categories (both default and user-created) available to the user")
    public ResponseEntity<List<CategoryDto>> getAll(@RequestParam(required = false) String type) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<Category> categories = categoryService.findByUser(user.getId());
        return ResponseEntity.ok(categories.stream().map(categoryMapper::toDto).collect(Collectors.toList()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a category", description = "Update a user-created category (cannot update default categories)")
    public ResponseEntity<CategoryDto> update(@PathVariable UUID id, @RequestBody CategoryCreateRequestDto dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setType(dto.getType());
        category.setColor(dto.getColor());
        category.setIcon(dto.getIcon());

        Category updated = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(categoryMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category", description = "Delete a user-created category")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
