package com.shopcart.shopcart_backend.controllers;

import com.shopcart.shopcart_backend.dto.OrderRequestDTO;
import com.shopcart.shopcart_backend.dto.OrderResponseDTO;
import com.shopcart.shopcart_backend.entities.OrderStatus;
import com.shopcart.shopcart_backend.entities.Role;
import com.shopcart.shopcart_backend.entities.User;
import com.shopcart.shopcart_backend.repositories.OrderRepository;
import com.shopcart.shopcart_backend.repositories.UserRepository;
import com.shopcart.shopcart_backend.services.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    // ✅ Place an order
    @PostMapping("/place")
    public ResponseEntity<OrderResponseDTO> placeOrder(@RequestBody OrderRequestDTO request,
                                                       Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

    if(currentUser.getRole() != Role.USER){
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(null); // Admin/Super Admin cannot place orders
    }

    OrderResponseDTO response = orderService.placeOrder(currentUser.getEmail(), request);
    return ResponseEntity.ok(response);
    }

    // ✅ Get all orders for current user
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(Authentication authentication) {
        String email = authentication.getName();
        List<OrderResponseDTO> orders = orderService.getOrdersByUser(email);
        return ResponseEntity.ok(orders);
    }


@GetMapping("/admin")
public ResponseEntity<?> getAdminOrders(Authentication authentication) {
    User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (currentUser.getRole() == Role.SUPER_ADMIN) {
        // Super admin → all orders
        return ResponseEntity.ok(orderService.getAllOrders());
    } 
    else if (currentUser.getRole() == Role.ADMIN) {
        // Admin → only own product orders
        return ResponseEntity.ok(orderService.getOrdersForAdmin(currentUser.getId()));
    } 
    else {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("You are not authorized to view this resource");
    }
}






    // ✅ Get single order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long orderId,
                                                         Authentication authentication) {
        String email = authentication.getName();
        OrderResponseDTO order = orderService.getOrderById(email, orderId);
        return ResponseEntity.ok(order);
    }

    // ✅ Update order status (Admin only)
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> updateOrderStatus(@PathVariable Long orderId,
                                                    @RequestParam OrderStatus status) {
        orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok("Order status updated successfully.");
    }
}
