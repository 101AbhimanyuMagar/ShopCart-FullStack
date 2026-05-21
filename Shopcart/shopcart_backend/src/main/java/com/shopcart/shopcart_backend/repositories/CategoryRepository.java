package com.shopcart.shopcart_backend.repositories;

import com.shopcart.shopcart_backend.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
