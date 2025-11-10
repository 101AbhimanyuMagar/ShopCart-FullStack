package com.shopcart.shopcart_backend.services;

import com.shopcart.shopcart_backend.dto.AdminDashboardDTO;
import com.shopcart.shopcart_backend.entities.OrderStatus;
import com.shopcart.shopcart_backend.repositories.OrderRepository;
import com.shopcart.shopcart_backend.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;

    @Override
    public AdminDashboardDTO getAdminMetrics() {
        long totalUsers = userRepository.count();
        long totalOrders = orderRepository.count();
        double totalRevenue = orderRepository.findAll().stream()
                .mapToDouble(order -> order.getTotalAmount())
                .sum();

        long placed = orderRepository.countByStatus(OrderStatus.PLACED);
        long shipped = orderRepository.countByStatus(OrderStatus.SHIPPED);
        long delivered = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long cancelled = orderRepository.countByStatus(OrderStatus.CANCELLED);

        return AdminDashboardDTO.builder()
                .totalUsers(totalUsers)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .placedOrders(placed)
                .shippedOrders(shipped)
                .deliveredOrders(delivered)
                .cancelledOrders(cancelled)
                .build();
    }
}
