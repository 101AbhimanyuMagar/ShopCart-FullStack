package com.shopcart.shopcart_backend.dto;

import com.shopcart.shopcart_backend.entities.Product;
import com.shopcart.shopcart_backend.entities.Discount;
import com.shopcart.shopcart_backend.entities.Category;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private double price; // effective (after discount)
    private double originalPrice; // actual/original
    private int stock;
    private String imageUrl;
    private DiscountDTO discount;
    private Category category; // ✅ include category info

    public static ProductResponseDTO from(Product product) {
        double effectivePrice;
        Discount activeDiscount = null;
        Date now = new Date();

        // ✅ find valid discount if available
        activeDiscount = product.getDiscounts() != null
                ? product.getDiscounts().stream()
                    .filter(Discount::isActive)
                    .filter(d -> {
                        Date start = d.getStartDate();
                        Date end = d.getEndDate();
                        boolean withinStart = (start == null) || !now.before(start);
                        boolean withinEnd = (end == null) || !now.after(end);
                        return withinStart && withinEnd;
                    })
                    .findFirst()
                    .orElse(null)
                : null;

        if (activeDiscount != null) {
            effectivePrice = product.getPrice() - (product.getPrice() * activeDiscount.getPercentage() / 100);
        } else {
            effectivePrice = product.getPrice();
        }

        DiscountDTO discountDTO = null;
        if (activeDiscount != null) {
            discountDTO = DiscountDTO.builder()
                    .percentage(activeDiscount.getPercentage())
                    .endDate(activeDiscount.getEndDate())
                    .active(activeDiscount.isActive())
                    .build();
        }

        // ✅ build response
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .price(effectivePrice)
                .originalPrice(product.getPrice())
                .discount(discountDTO)
                .category(product.getCategory()) // ✅ now included
                .build();
    }
}
