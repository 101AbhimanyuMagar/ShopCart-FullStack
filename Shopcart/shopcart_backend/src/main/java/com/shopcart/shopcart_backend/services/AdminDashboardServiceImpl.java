package com.shopcart.shopcart_backend.services;

import com.shopcart.shopcart_backend.dto.AdminDashboardDTO;
import com.shopcart.shopcart_backend.entities.OrderItem;
import com.shopcart.shopcart_backend.entities.OrderStatus;
import com.shopcart.shopcart_backend.entities.User;
import com.shopcart.shopcart_backend.repositories.OrderRepository;
import com.shopcart.shopcart_backend.repositories.ProductRepository;
import com.shopcart.shopcart_backend.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;

    @Override
    public AdminDashboardDTO getAdminMetrics() {
        long totalUsers = userRepository.count();
        long totalOrders = orderRepository.count();
        double totalRevenue = orderRepository.findAll().stream()
                .mapToDouble(o -> o.getTotalAmount())
                .sum();

        return AdminDashboardDTO.builder()
                .totalUsers(totalUsers)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .placedOrders(orderRepository.countByStatus(OrderStatus.PLACED))
                .shippedOrders(orderRepository.countByStatus(OrderStatus.SHIPPED))
                .deliveredOrders(orderRepository.countByStatus(OrderStatus.DELIVERED))
                .cancelledOrders(orderRepository.countByStatus(OrderStatus.CANCELLED))
                .build();
    }

   @Override
public AdminDashboardDTO getMetricsForAdmin(String email) {
    User admin = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Admin not found"));

    // Make sure you fetch latest orders/items
    List<OrderItem> adminItems = orderRepository.findOrderItemsByAdminId(admin.getId());

    long totalOrders = orderRepository.findOrdersByAdminId(admin.getId()).size();

    double totalRevenue = Optional.ofNullable(
        orderRepository.findTotalRevenueByAdminIdAndStatus(admin.getId(), OrderStatus.DELIVERED)
    ).orElse(0.0);

    long placed = adminItems.stream().filter(i -> i.getStatus() == OrderStatus.PLACED).count();
    long shipped = adminItems.stream().filter(i -> i.getStatus() == OrderStatus.SHIPPED).count();
    long delivered = adminItems.stream().filter(i -> i.getStatus() == OrderStatus.DELIVERED).count();
    long cancelled = adminItems.stream().filter(i -> i.getStatus() == OrderStatus.CANCELLED).count();

    return AdminDashboardDTO.builder()
            .totalOrders(totalOrders)
            .totalRevenue(totalRevenue)
            .placedOrders(placed)
            .shippedOrders(shipped)
            .deliveredOrders(delivered)
            .cancelledOrders(cancelled)
            .build();
}


}

