package com.shopcart.shopcart_backend.dto;


import com.shopcart.shopcart_backend.entities.CartItem;
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

    public static CartItemResponseDTO from(CartItem cartItem) {
        return CartItemResponseDTO.builder()
                .id(cartItem.getId())
                .product(ProductResponseDTO.from(cartItem.getProduct()))
                .quantity(cartItem.getQuantity())
                .total(cartItem.getQuantity() * cartItem.getProduct().getPrice())
                .build();
    }
}

