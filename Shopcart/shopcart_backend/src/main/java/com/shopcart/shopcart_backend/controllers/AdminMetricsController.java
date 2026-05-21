package com.shopcart.shopcart_backend.controllers;

import com.shopcart.shopcart_backend.dto.AdminDashboardDTO;
import com.shopcart.shopcart_backend.services.AdminDashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/metrics")
@RequiredArgsConstructor
@Slf4j
public class AdminMetricsController {

    private final AdminDashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public AdminDashboardDTO getMetrics(Authentication authentication) {

        String email = authentication.getName();

        log.info("📊 Admin metrics requested by: {}", email);

        boolean isSuperAdmin = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_SUPER_ADMIN"));

        if (isSuperAdmin) {
            return dashboardService.getAdminMetrics();
        }

        return dashboardService.getMetricsForAdmin(email);
    }
}