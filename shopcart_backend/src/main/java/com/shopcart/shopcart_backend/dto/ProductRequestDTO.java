package com.shopcart.shopcart_backend.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDTO {
    private String name;
    private String description;
    private double price;
    private int stock;
    private String imageUrl;
}

