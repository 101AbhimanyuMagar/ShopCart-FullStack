package com.shopcart.shopcart_backend.repositories;

import com.shopcart.shopcart_backend.entities.Order;
import com.shopcart.shopcart_backend.entities.OrderItem;
import com.shopcart.shopcart_backend.entities.OrderStatus;
import com.shopcart.shopcart_backend.entities.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    

    // ✅ Fetch all orders of a specific user
    List<Order> findByUser(User user);

    // ✅ Count total orders by status
    long countByStatus(OrderStatus status);

    // ✅ Fetch all orders containing products added by a specific admin
    @Query("""
        SELECT DISTINCT o 
        FROM Order o 
        JOIN o.orderItems i 
        JOIN i.product p 
        WHERE p.addedBy.id = :adminId
    """)
    List<Order> findOrdersByAdminId(@Param("adminId") Long adminId);

    // ✅ Fetch all individual order items related to a specific admin’s products
    @Query("""
        SELECT i 
        FROM OrderItem i 
        WHERE i.product.addedBy.id = :adminId
    """)
    List<OrderItem> findOrderItemsByAdminId(@Param("adminId") Long adminId);

@Query("""
    SELECT COALESCE(SUM(i.total), 0)
    FROM OrderItem i
    WHERE i.product.addedBy.id = :adminId
      AND i.status = :status
""")
Double findTotalRevenueByAdminIdAndStatus(@Param("adminId") Long adminId,
                                          @Param("status") OrderStatus status);

}
