package com.shopcart.shopcart_backend.dto;

import com.shopcart.shopcart_backend.entities.OrderItem;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponseDTO {
    private Long id;
    private String productName;
    private double price;
    private int quantity;
    private double total;
    private String status;

    public static OrderItemResponseDTO from(OrderItem item) {
        return OrderItemResponseDTO.builder()
                .id(item.getId())
                .productName(item.getProduct().getName())
                .price(item.getProduct().getPrice())
                .quantity(item.getQuantity())
                .total(item.getTotal())
                .status(item.getStatus() != null ? item.getStatus().name() : "PLACED")
                .build();
    }
}
