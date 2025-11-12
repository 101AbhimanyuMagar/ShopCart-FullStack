package com.shopcart.shopcart_backend.controllers;

import com.shopcart.shopcart_backend.dto.AdminDashboardDTO;
import com.shopcart.shopcart_backend.services.AdminDashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/metrics")
public class AdminMetricsController {

    @Autowired
    private AdminDashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public AdminDashboardDTO getMetrics(Authentication authentication) {
        String email = authentication.getName(); // username/email from token

        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_SUPER_ADMIN"));

        if (isSuperAdmin) {
            return dashboardService.getAdminMetrics();
        } else {
            return dashboardService.getMetricsForAdmin(email);
        }
    }
}
