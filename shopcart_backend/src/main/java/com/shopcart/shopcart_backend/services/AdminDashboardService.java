package com.shopcart.shopcart_backend.services;

import com.shopcart.shopcart_backend.dto.AdminDashboardDTO;

public interface AdminDashboardService {
    AdminDashboardDTO getAdminMetrics(); // for SUPER_ADMIN
    AdminDashboardDTO getMetricsForAdmin(String email); // for ADMIN
}
