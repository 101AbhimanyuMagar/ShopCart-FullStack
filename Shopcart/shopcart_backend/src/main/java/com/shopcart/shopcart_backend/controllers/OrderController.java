package com.shopcart.shopcart_backend.controllers;

import com.shopcart.shopcart_backend.dto.OrderRequestDTO;
import com.shopcart.shopcart_backend.dto.OrderResponseDTO;
import com.shopcart.shopcart_backend.entities.OrderStatus;
import com.shopcart.shopcart_backend.services.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /*
     * Place order
     */
    @PostMapping("/place")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponseDTO> placeOrder(
            @RequestBody OrderRequestDTO request,
            Authentication authentication) {

        String email = authentication.getName();

        log.info("🛒 Placing order for user: {}", email);

        OrderResponseDTO response =
                orderService.placeOrder(email, request);

        log.info("✅ Order placed successfully | Order ID: {}",
                response.getId());

        return ResponseEntity.ok(response);
    }

    /*
     * Get current user orders
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(
            Authentication authentication) {

        String email = authentication.getName();

        log.info("📦 Fetching orders for user: {}", email);

        return ResponseEntity.ok(
                orderService.getOrdersByUser(email)
        );
    }

    /*
     * Admin/Super Admin Orders
     */
    @GetMapping("/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public ResponseEntity<List<OrderResponseDTO>> getAdminOrders(
        Authentication authentication) {

    String email = authentication.getName();

    log.info("📊 Admin order access by: {}", email);

    List<OrderResponseDTO> orders =
            orderService.getOrdersForAdminPanel(email);

    log.info("✅ Total admin orders fetched: {}", orders.size());

    return ResponseEntity.ok(orders);
}
    /*
     * Get single order
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @PathVariable Long orderId,
            Authentication authentication) {

        String email = authentication.getName();

        log.info("🔍 Fetching order {} for user {}",
                orderId,
                email);

        return ResponseEntity.ok(
                orderService.getOrderById(email, orderId)
        );
    }

    /*
     * Update order status
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        log.info("🟡 Updating order status | Order ID: {} | Status: {}",
                orderId,
                status);

        orderService.updateOrderStatus(orderId, status);

        log.info("✅ Order status updated successfully");

        return ResponseEntity.ok(
                "Order status updated successfully."
        );
    }

    /*
     * Cancel order item
     */
    @PutMapping("/{orderId}/items/{itemId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> cancelOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            Authentication authentication) {

        String email = authentication.getName();

        log.info("❌ Cancel order item request | User: {} | Order: {} | Item: {}",
                email,
                orderId,
                itemId);

        orderService.cancelOrderItem(email, orderId, itemId);

        log.info("✅ Order item cancelled successfully");

        return ResponseEntity.ok(
                "Order item cancelled successfully."
        );
    }

    /*
     * Download invoice PDF
     */
    @GetMapping("/{orderId}/invoice")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<byte[]> downloadInvoice(
            @PathVariable Long orderId,
            Authentication authentication) {

        String email = authentication.getName();

        log.info("📄 Invoice download request | User: {} | Order: {}",
                email,
                orderId);

        byte[] pdf =
                orderService.generateInvoicePdf(orderId, email);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_PDF);

        headers.setContentDispositionFormData(
                "attachment",
                "invoice_" + orderId + ".pdf"
        );

        log.info("✅ Invoice generated successfully");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}