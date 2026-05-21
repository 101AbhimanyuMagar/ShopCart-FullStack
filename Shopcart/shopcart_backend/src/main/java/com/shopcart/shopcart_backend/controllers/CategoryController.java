package com.shopcart.shopcart_backend.controllers;

import com.shopcart.shopcart_backend.entities.Category;
import com.shopcart.shopcart_backend.services.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    /*
     * Get all categories
     */
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {

        log.info("📂 Fetching all categories");

        List<Category> categories =
                categoryService.getAllCategories();

        log.info("✅ Total categories fetched: {}",
                categories.size());

        return ResponseEntity.ok(categories);
    }

    /*
     * Add category
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<Category> addCategory(
            @RequestBody Category category) {

        log.info("🟢 Adding new category: {}",
                category.getName());

        Category savedCategory =
                categoryService.addCategory(category);

        log.info("✅ Category added successfully with ID: {}",
                savedCategory.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedCategory);
    }

    /*
     * Update category
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @RequestBody Category category) {

        log.info("🟡 Updating category ID: {}",
                id);

        Category updatedCategory =
                categoryService.updateCategory(id, category);

        log.info("✅ Category updated successfully: {}",
                updatedCategory.getName());

        return ResponseEntity.ok(updatedCategory);
    }

    /*
     * Delete category
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id) {

        log.info("❌ Deleting category ID: {}",
                id);

        categoryService.deleteCategory(id);

        log.info("✅ Category deleted successfully: {}",
                id);

        return ResponseEntity.noContent().build();
    }
}