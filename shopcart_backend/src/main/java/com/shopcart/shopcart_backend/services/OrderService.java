package com.shopcart.shopcart_backend.services;

import com.shopcart.shopcart_backend.dto.OrderRequestDTO;
import com.shopcart.shopcart_backend.dto.OrderResponseDTO;
import com.shopcart.shopcart_backend.entities.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponseDTO placeOrder(String email, OrderRequestDTO request);

    List<OrderResponseDTO> getOrdersByUser(String email);
    List<OrderResponseDTO> getAllOrders(); // For admin
    OrderResponseDTO getOrderById(String email, Long orderId);
    void updateOrderStatus(Long orderId, OrderStatus status);
    void updateOrderItemStatus(Long itemId, OrderStatus status);
    List<OrderResponseDTO> getOrdersForAdmin(Long adminId);

}
