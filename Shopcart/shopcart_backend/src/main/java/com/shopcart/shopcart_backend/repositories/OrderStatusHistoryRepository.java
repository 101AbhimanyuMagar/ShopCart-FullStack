package com.shopcart.shopcart_backend.repositories;

import com.shopcart.shopcart_backend.entities.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
}
