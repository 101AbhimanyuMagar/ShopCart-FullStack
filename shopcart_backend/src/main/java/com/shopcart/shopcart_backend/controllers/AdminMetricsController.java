package com.shopcart.shopcart_backend.controllers;

import com.shopcart.shopcart_backend.dto.AdminDashboardDTO;
import com.shopcart.shopcart_backend.services.AdminDashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/metrics")
@PreAuthorize("hasRole('SUPER_ADMIN')")  // ✅ Only Super Admin can access
public class AdminMetricsController {

    @Autowired
    private AdminDashboardService dashboardService;

    @GetMapping
    public AdminDashboardDTO getMetrics() {
        return dashboardService.getAdminMetrics();
    }
}
