package com.shopcart.shopcart_backend.dto;


import com.shopcart.shopcart_backend.entities.CartItem;
import com.shopcart.shopcart_backend.services.ProductService;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponseDTO {
    private Long id;
    private ProductResponseDTO product;
    private Integer quantity;
    private double total;

public static CartItemResponseDTO from(CartItem cartItem, ProductService productService) {
    ProductResponseDTO productDTO = ProductResponseDTO.from(cartItem.getProduct());
    double effectivePrice = productService.getEffectivePrice(cartItem.getProduct());
    productDTO.setPrice(effectivePrice);

    return CartItemResponseDTO.builder()
            .id(cartItem.getId())
            .product(productDTO)
            .quantity(cartItem.getQuantity())
            .total(cartItem.getQuantity() * effectivePrice)
            .build();
}



}

