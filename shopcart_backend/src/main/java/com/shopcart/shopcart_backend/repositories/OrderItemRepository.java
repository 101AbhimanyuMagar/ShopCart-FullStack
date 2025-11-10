package com.shopcart.shopcart_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopcart.shopcart_backend.entities.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}
