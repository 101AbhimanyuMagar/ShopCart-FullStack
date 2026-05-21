package com.shopcart.shopcart_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopcart.shopcart_backend.entities.CartItem;
import com.shopcart.shopcart_backend.entities.User;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
}