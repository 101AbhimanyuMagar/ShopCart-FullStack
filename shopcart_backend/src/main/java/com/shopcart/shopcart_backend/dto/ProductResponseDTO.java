package com.shopcart.shopcart_backend.dto;

import com.shopcart.shopcart_backend.entities.Product;
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
    private double price;
    private int stock;
    private String imageUrl;
    private String addedByName;

    private Date createdAt;
    private Date updatedAt;

    // ✅ Static converter method
    public static ProductResponseDTO from(Product product) {
        if (product == null) return null;

        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .addedByName(product.getAddedBy() != null ? product.getAddedBy().getName() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
