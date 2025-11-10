package com.shopcart.shopcart_backend.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminDashboardDTO {
    private long totalUsers;
    private long totalOrders;
    private double totalRevenue;
    private long placedOrders;
    private long shippedOrders;
    private long deliveredOrders;
    private long cancelledOrders;
}
