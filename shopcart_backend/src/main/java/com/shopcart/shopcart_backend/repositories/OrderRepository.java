package com.shopcart.shopcart_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopcart.shopcart_backend.entities.Order;
import com.shopcart.shopcart_backend.entities.OrderItem;
import com.shopcart.shopcart_backend.entities.OrderStatus;
import com.shopcart.shopcart_backend.entities.User;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    long countByStatus(OrderStatus status);
// Orders containing products added by a specific admin
@Query("""
    SELECT DISTINCT o 
    FROM Order o 
    JOIN FETCH o.orderItems i 
    JOIN FETCH i.product p 
    WHERE p.addedBy.id = :adminId
""")
List<Order> findOrdersByAdminId(@Param("adminId") Long adminId);

@Query("SELECT i FROM OrderItem i WHERE i.product.addedBy.id = :adminId")
List<OrderItem> findByAdminId(@Param("adminId") Long adminId);




}
