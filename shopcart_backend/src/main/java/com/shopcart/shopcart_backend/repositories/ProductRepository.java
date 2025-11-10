package com.shopcart.shopcart_backend.repositories;

import com.shopcart.shopcart_backend.entities.Order;
import com.shopcart.shopcart_backend.entities.Product;
import com.shopcart.shopcart_backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByAddedBy(User addedBy);

    List<Product> findByAddedByEmail(String email);

    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN o.orderItems i " +
           "WHERE i.product.addedBy.id = :adminId")
    List<Order> findOrdersByAdminProducts(@Param("adminId") Long adminId);
}
