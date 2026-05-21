package com.shopcart.shopcart_backend.services;

import com.shopcart.shopcart_backend.entities.Category;
import java.util.List;

public interface CategoryService {
    Category addCategory(Category category);
    Category updateCategory(Long id, Category category);
    void deleteCategory(Long id);
    List<Category> getAllCategories();
    Category getCategoryById(Long id);
}
