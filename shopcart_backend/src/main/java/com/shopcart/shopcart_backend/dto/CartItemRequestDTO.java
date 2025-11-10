package com.shopcart.shopcart_backend.dto;
import lombok.*;

@Getter
@Setter
public class CartItemRequestDTO {
    private Long productId;
    private Integer quantity;
}

