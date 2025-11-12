package com.shopcart.shopcart_backend.repositories;

import com.shopcart.shopcart_backend.entities.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Long> {

    // ✅ Find currently active discount for a product (within valid date range)
    @Query("""
        SELECT d FROM Discount d
        WHERE d.product.id = :productId
          AND d.active = true
          AND (d.startDate IS NULL OR d.startDate <= :currentDate)
          AND (d.endDate IS NULL OR d.endDate >= :currentDate)
    """)
    Optional<Discount> findActiveDiscountForProduct(
            @Param("productId") Long productId,
            @Param("currentDate") Date currentDate
    );

    // ✅ Optional: find all active discounts
    @Query("""
        SELECT d FROM Discount d
        WHERE d.active = true
          AND (d.endDate IS NULL OR d.endDate >= :currentDate)
    """)
    java.util.List<Discount> findAllActiveDiscounts(@Param("currentDate") Date currentDate);
}
