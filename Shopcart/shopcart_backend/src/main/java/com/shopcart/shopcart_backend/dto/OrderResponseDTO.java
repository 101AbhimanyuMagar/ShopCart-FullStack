package com.shopcart.shopcart_backend.dto;


import java.util.*;
import java.util.stream.Collectors;

import com.shopcart.shopcart_backend.entities.Order;
import com.shopcart.shopcart_backend.entities.OrderStatus;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {
    private Long id;
    private Date createdAt;
    private OrderStatus status;
    private double totalAmount;
    private String userName;
    private String paymentMethod;
private String transactionId;
private String paymentStatus;

    private List<OrderItemResponseDTO> orderItems;

    public static OrderResponseDTO from(Order order) {
    return OrderResponseDTO.builder()
            .id(order.getId())
            .createdAt(order.getCreatedAt())
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .userName(order.getUser().getName())
            .paymentMethod(order.getPaymentMethod())     // ✅ include payment method
            .transactionId(order.getTransactionId())     // ✅ include txn ID
            .paymentStatus(order.getPaymentStatus())     // ✅ include payment status
            .orderItems(order.getOrderItems()
                             .stream()
                             .map(OrderItemResponseDTO::from)
                             .collect(Collectors.toList()))
            .build();
}

}


